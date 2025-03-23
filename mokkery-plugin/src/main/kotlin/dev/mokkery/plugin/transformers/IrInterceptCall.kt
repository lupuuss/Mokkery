package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.IrMokkeryKind
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.core.getProperty
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.eraseTypeParametersCompat
import dev.mokkery.plugin.ir.getField
import dev.mokkery.plugin.ir.indexIfParameterOrNull
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irCallListOf
import dev.mokkery.plugin.ir.irCallMapOf
import dev.mokkery.plugin.ir.irInvoke
import dev.mokkery.plugin.ir.irLambda
import dev.mokkery.plugin.ir.isSuperCallFor
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.nonDispatchParametersCompat
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irAs
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irGetField
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionExpression
import org.jetbrains.kotlin.ir.expressions.putArgument
import org.jetbrains.kotlin.ir.types.IrType
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
    mokkeryKind: IrMokkeryKind,
): IrCall {
    val parentClass = function.parentAsClass
    return irInterceptCall(
        transformer = transformer,
        mokkeryInstance = irGet(function.dispatchReceiverParameter!!),
        function = function,
        mokkeryKind = mokkeryKind,
        typeToKClassMapper = {
            val index = it.indexIfParameterOrNull(parentClass)
            if (index != null) {
                irGetField(
                    receiver = irGet(function.dispatchReceiverParameter!!),
                    field = parentClass.getField(Mokkery.Fields.typeArg(index))!!
                )
            } else {
                kClassReference(it.eraseTypeParametersCompat())
            }
        }
    )
}

fun IrBlockBodyBuilder.irInterceptCall(
    transformer: TransformerScope,
    mokkeryInstance: IrExpression,
    function: IrSimpleFunction,
    mokkeryKind: IrMokkeryKind,
    typeToKClassMapper: IrBuilderWithScope.(IrType) -> IrExpression,
): IrCall {
    val interceptorClass = transformer.getClass(Mokkery.Class.MokkeryCallInterceptor).symbol
    val instanceScopeClass = transformer.getClass(Mokkery.Class.MokkeryInstanceScope)
    val getSpiedObject = transformer.getProperty(Mokkery.Property.spiedObject).getter!!
    val interceptFun = interceptorClass
        .functions
        .first { it.owner.name.asString() == "intercept" && it.owner.isSuspend == function.isSuspend }
    return irCall(interceptFun) {
        dispatchReceiver = instanceScopeClass
            .getPropertyGetter("mokkeryInterceptor")!!
            .let(::irCall)
            .apply { dispatchReceiver = mokkeryInstance }
        val scopeCreationFun = when {
            function.isSuspend -> Mokkery.Function.createMokkerySuspendCallScope
            else -> Mokkery.Function.createMokkeryBlockingCallScope
        }
        val scopeCreationCall = irCall(transformer.getFunction(scopeCreationFun)) {
            extensionReceiver = mokkeryInstance
            putValueArgument(0, irString(function.name.asString()))
            putValueArgument(1, typeToKClassMapper(function.returnType))
            putValueArgument(2, irCallArgsList(transformer, function.nonDispatchParametersCompat, typeToKClassMapper))
            putValueArgument(3, irCallSupersMap(transformer, function))
            if (mokkeryKind == IrMokkeryKind.Spy) {
                val spiedObjectGet = irCall(getSpiedObject) { extensionReceiver = mokkeryInstance }
                // js function does not have a dispatch parameter
                val spyLambda = if (function.dispatchReceiverParameter == null) {
                    irLambdaSpyFunctionCall(transformer, spiedObjectGet, function)
                } else {
                    irLambdaSpyMethodCall(transformer, spiedObjectGet, function)
                }
                putValueArgument(4, spyLambda)
            }
        }
        putValueArgument(0, scopeCreationCall)
    }
}

private fun IrBuilderWithScope.irCallArgsList(
    scope: TransformerScope,
    parameters: List<IrValueParameter>,
    typeToKClassExpressionMapper: IrBuilderWithScope.(IrType) -> IrExpression
): IrCall {
    val callArgClass = scope.getClass(Mokkery.Class.CallArgument)
    val callArgs = parameters
        .map {
            irCallConstructor(callArgClass.constructors.take(2).last()) {
                putValueArgument(0, irGet(it))
                putValueArgument(1, irString(it.name.asString()))
                putValueArgument(2, typeToKClassExpressionMapper(it.type))
                putValueArgument(3, irBoolean(it.isVararg))
            }
        }
    return irCallListOf(scope, callArgClass.defaultType, callArgs)
}

private fun IrBuilderWithScope.irCallSupersMap(transformer: TransformerScope, function: IrSimpleFunction): IrCall? {
    val supers = function.overriddenSymbols
        .filter { it.owner.isSuperCallFor(function) }
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
            function.typeParameters.forEachIndexed { i, type -> putTypeArgument(i, type.defaultType) }
            superFunction.nonDispatchParametersCompat.forEachIndexed { index, irValueParameter ->
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


private fun IrBlockBodyBuilder.irLambdaSpyMethodCall(
    transformer: TransformerScope,
    spyObjectDelegate: IrExpression,
    function: IrSimpleFunction,
): IrFunctionExpression {
    val pluginContext = transformer.pluginContext
    val lambdaType = pluginContext
        .irBuiltIns
        .let { if (function.isSuspend) it.suspendFunctionN(1) else it.functionN(1) }
        .typeWith(pluginContext.irBuiltIns.listClass.owner.defaultTypeErased, function.returnType)
    return irLambda(
        returnType = function.returnType,
        lambdaType = lambdaType,
        parent = parent,
    ) { lambda ->
        val spyFun = function.overriddenSymbols.first().owner
        val typesMap = makeTypeParameterSubstitutionMap(spyFun, function)
        val spyCall = irCall(spyFun, spyFun.returnType.substitute(typesMap)) {
            dispatchReceiver = spyObjectDelegate
            function.typeParameters.forEachIndexed { i, type -> putTypeArgument(i, type.defaultType) }
            spyFun.nonDispatchParametersCompat.forEachIndexed { index, irValueParameter ->
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
        +irReturn(spyCall)
    }
}

private fun IrBlockBodyBuilder.irLambdaSpyFunctionCall(
    transformer: TransformerScope,
    delegateLambda: IrExpression,
    function: IrSimpleFunction,
): IrFunctionExpression {
    val pluginContext = transformer.pluginContext
    val lambdaType = pluginContext
        .irBuiltIns
        .let { if (function.isSuspend) it.suspendFunctionN(1) else it.functionN(1) }
        .typeWith(pluginContext.irBuiltIns.listClass.owner.defaultTypeErased, function.returnType)
    return irLambda(
        returnType = function.returnType,
        lambdaType = lambdaType,
        parent = parent,
    ) { lambda ->
        val args = Array(function.valueParameters.size) {
            irCall(context.irBuiltIns.listClass.owner.getSimpleFunction("get")!!) {
                dispatchReceiver = irGet(lambda.valueParameters[0])
                putValueArgument(0, irInt(it))
            }
        }
        +irReturn(irInvoke(function = delegateLambda, isSuspend = lambda.isSuspend, args = args))
    }
}
