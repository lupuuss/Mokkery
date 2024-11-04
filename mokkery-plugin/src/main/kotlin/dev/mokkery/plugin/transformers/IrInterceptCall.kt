package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.allowIndirectSuperCalls
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irCallListOf
import dev.mokkery.plugin.ir.irCallMapOf
import dev.mokkery.plugin.ir.irLambda
import dev.mokkery.plugin.ir.isJvmBinarySafeSuperCall
import dev.mokkery.plugin.ir.kClassReference
import org.jetbrains.kotlin.backend.jvm.fullValueParameterList
import org.jetbrains.kotlin.backend.jvm.ir.eraseTypeParameters
import org.jetbrains.kotlin.config.JvmAnalysisFlags
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.putArgument
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.makeTypeParameterSubstitutionMap
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.substitute

fun IrBlockBodyBuilder.irInterceptMethod(
    transformer: TransformerScope,
    function: IrSimpleFunction,
    irCallSpyLambda: IrExpression? = null,
): IrCall = irInterceptCall(
    transformer = transformer,
    mokkeryInstance = irGet(function.dispatchReceiverParameter!!),
    function = function,
    irCallSpyLambda = irCallSpyLambda,
)

fun IrBlockBodyBuilder.irInterceptCall(
    transformer: TransformerScope,
    mokkeryInstance: IrExpression,
    function: IrSimpleFunction,
    irCallSpyLambda: IrExpression? = null
): IrCall {
    val interceptorClass = transformer.getClass(Mokkery.Class.MokkeryCallInterceptor).symbol
    val mokkeryInstanceClass = transformer.getClass(Mokkery.Class.MokkeryInstance)
    val interceptFun = interceptorClass
        .functions
        .first { it.owner.name.asString() == "intercept" && it.owner.isSuspend == function.isSuspend }
    return irCall(interceptFun) {
        dispatchReceiver = mokkeryInstanceClass
            .getPropertyGetter("_mokkeryInterceptor")!!
            .let(::irCall)
            .apply { dispatchReceiver = mokkeryInstance }
        val scopeCreationFun = when {
            function.isSuspend -> Mokkery.Function.createMokkerySuspendCallScope
            else -> Mokkery.Function.createMokkeryBlockingCallScope
        }
        val scopeCreationCall = irCall(transformer.getFunction(scopeCreationFun)) {
            putValueArgument(0, mokkeryInstance)
            putValueArgument(1, irString(function.name.asString()))
            putValueArgument(2, kClassReference(function.returnType.eraseTypeParameters()))
            putValueArgument(3, irCallArgsList(transformer, function.fullValueParameterList))
            putValueArgument(4, irCallSupersMap(transformer, function))
            if (irCallSpyLambda != null) putValueArgument(5, irCallSpyLambda)
        }
        putValueArgument(0, scopeCreationCall)
    }
}

private fun IrBuilderWithScope.irCallArgsList(scope: TransformerScope, parameters: List<IrValueParameter>): IrCall {
    val callArgClass = scope.getClass(Mokkery.Class.CallArgument)
    val callArgs = parameters
        .map {
            irCallConstructor(callArgClass.constructors.take(2).last()) {
                putValueArgument(0, irGet(it))
                putValueArgument(1, irString(it.name.asString()))
                putValueArgument(2, kClassReference(it.type.eraseTypeParameters()))
                putValueArgument(3, irBoolean(it.isVararg))
            }
        }
    return irCallListOf(scope, callArgClass.defaultType, callArgs)
}

private fun IrBuilderWithScope.irCallSupersMap(transformer: TransformerScope, function: IrSimpleFunction): IrCall? {
    val allowIndirectSuperCalls = transformer.allowIndirectSuperCalls
    val defaultMode = transformer.pluginContext.languageVersionSettings.getFlag(JvmAnalysisFlags.jvmDefaultMode)
    val supers = function.overriddenSymbols
        .filter { it.owner.isJvmBinarySafeSuperCall(function, defaultMode, allowIndirectSuperCalls) }
        .takeIf { it.isNotEmpty() }
        ?.map { it.owner }
        ?: return null
    val superLambdas = supers.map { superFunction ->
        val kClass = kClassReference(superFunction.parentAsClass.defaultType)
        val lambda = createSuperCallLambda(transformer, function, superFunction)
        kClass to lambda
    }
    return irCallMapOf(transformer, superLambdas)
}

private fun IrBuilderWithScope.createSuperCallLambda(
    transformer: TransformerScope,
    function: IrSimpleFunction,
    superFunction: IrSimpleFunction
): IrExpression {
    val pluginContext = transformer.pluginContext
    val typesMap = makeTypeParameterSubstitutionMap(superFunction, function)
    val returnType = superFunction.returnType.substitute(typesMap)
    val lambdaType = pluginContext
        .irBuiltIns
        .let { if (function.isSuspend) it.suspendFunctionN(1) else it.functionN(1) }
        .typeWith(pluginContext.irBuiltIns.listClass.owner.defaultTypeErased, returnType)
    return irLambda(
        returnType = returnType,
        lambdaType = lambdaType,
        parent = parent,
    ) { lambda ->
        val superCall = irCall(
            symbol = superFunction.symbol,
            superQualifierSymbol = superFunction.parentAsClass.symbol,
            type = returnType,
        ) {
            dispatchReceiver = irGet(function.dispatchReceiverParameter!!)
            contextReceiversCount = superFunction.contextReceiverParametersCount
            function.typeParameters.forEachIndexed { i, type -> putTypeArgument(i, type.defaultType) }
            superFunction.fullValueParameterList.forEachIndexed { index, irValueParameter ->
                putArgument(
                    parameter = irValueParameter,
                    argument = irAs(
                        argument = irCall(context.irBuiltIns.listClass.owner.getSimpleFunction("get")!!) {
                            dispatchReceiver = irGet(lambda.valueParameters[0])
                            putValueArgument(0, irInt(index))
                        },
                        type = irValueParameter.type.substitute(typesMap)
                    )
                )
            }
        }
        +irReturn(superCall)
    }
}
