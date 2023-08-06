package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Kotlin
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.core.getFunction
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.irLambda
import dev.mokkery.plugin.ir.kClassReferenceUnified
import dev.mokkery.plugin.ir.nonGenericReturnTypeOrAny
import org.jetbrains.kotlin.backend.jvm.fullValueParameterList
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.builders.parent
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.getSimpleFunction
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.primaryConstructor

fun IrBlockBodyBuilder.irInterceptMethod(
    transformer: TransformerScope,
    function: IrSimpleFunction
): IrCall = irInterceptCall(
    transformer = transformer,
    mokkeryScope = irGet(function.dispatchReceiverParameter!!),
    function = function
)

fun IrBlockBodyBuilder.irInterceptCall(
    transformer: TransformerScope,
    mokkeryScope: IrExpression,
    function: IrSimpleFunction
): IrCall {
    val interceptorClass = transformer.getClass(Mokkery.Class.MokkeryInterceptor).symbol
    val interceptorScopeClass = transformer.getClass(Mokkery.Class.MokkeryInterceptorScope)
    val callContextClass = transformer.getClass(Mokkery.Class.CallContext)
    val pluginContext = transformer.pluginContext
    val interceptFun = if (function.isSuspend) {
        interceptorClass.functionByName("interceptSuspendCall")
    } else {
        interceptorClass.functionByName("interceptCall")
    }
    return irCall(interceptFun) {
        dispatchReceiver = interceptorScopeClass
            .getPropertyGetter("interceptor")!!
            .let(::irCall)
            .apply { dispatchReceiver = mokkeryScope }
        val contextCreationCall = irCallConstructor(callContextClass.primaryConstructor!!) {
            putValueArgument(0, mokkeryScope)
            putValueArgument(1, irString(function.name.asString()))
            putValueArgument(
                2,
                kClassReferenceUnified(pluginContext, function.nonGenericReturnTypeOrAny(pluginContext))
            )
            putValueArgument(3, irCallArgsList(transformer, function.fullValueParameterList))
            putValueArgument(4, irCallSupersMap(transformer, function))
        }
        putValueArgument(0, contextCreationCall)
    }
}

private fun IrBuilderWithScope.irCallArgsList(scope: TransformerScope, parameters: List<IrValueParameter>): IrCall {
    val callArgClass = scope.getClass(Mokkery.Class.CallArg)
    val pluginContext = scope.pluginContext
    val args = irVararg(
        elementType = callArgClass.defaultType,
        values = parameters
            .map {
                irCallConstructor(callArgClass.primaryConstructor!!) {
                    putValueArgument(0, irString(it.name.asString()))
                    putValueArgument(
                        1,
                        kClassReferenceUnified(pluginContext, it.nonGenericReturnTypeOrAny(pluginContext))
                    )
                    putValueArgument(2, irGet(it))
                    putValueArgument(3, irBoolean(it.isVararg))
                }
            }
    )
    val listOf = pluginContext.referenceFunctions(Kotlin.Name.listOf).first {
        it.owner.valueParameters.firstOrNull()?.isVararg == true
    }
    return irCall(listOf) {
        putValueArgument(0, args)
    }
}

private fun IrBuilderWithScope.irCallSupersMap(transformer: TransformerScope, function: IrSimpleFunction): IrCall? {
    val pluginContext = transformer.pluginContext
    val supers = function.overriddenSymbols
        .filter { it.owner.modality == Modality.OPEN }
        .takeIf { it.isNotEmpty() }
        ?.map { it.owner }
        ?: return null
    val mapOf = pluginContext
        .referenceFunctions(Kotlin.Name.mapOf)
        .first { it.owner.valueParameters.firstOrNull()?.isVararg == true }
    val superLambdas = supers.map { superFunction ->
        irCreatePair(
            transformer = transformer,
            first = kClassReferenceUnified(pluginContext, superFunction.parentAsClass.defaultType),
            second = createSuperCallLambda(transformer, function, superFunction)
        )
    }
    return irCall(mapOf) {
        val varargs = irVararg(
            elementType = transformer.getClass(Kotlin.Class.Pair).defaultType,
            values = superLambdas
        )
        putValueArgument(0, varargs)
    }
}

private fun IrBuilderWithScope.irCreatePair(
    transformer: TransformerScope,
    first: IrExpression,
    second: IrExpression
): IrExpression {
    return irCall(transformer.getFunction(Kotlin.Function.to)) {
        extensionReceiver = first
        putValueArgument(0, second)
    }
}

private fun IrBuilderWithScope.createSuperCallLambda(
    transformer: TransformerScope,
    function: IrSimpleFunction,
    superFunction: IrSimpleFunction
): IrExpression {
    val pluginContext = transformer.pluginContext
    val anyNType = pluginContext.irBuiltIns.anyNType
    val anyList = pluginContext.irBuiltIns.listClass.owner
    val lambdaType = pluginContext
        .irBuiltIns
        .let { if (function.isSuspend) it.suspendFunctionN(1) else it.functionN(1) }
        .typeWith(anyList.defaultTypeErased, anyNType)
    return irLambda(
        returnType = anyNType,
        lambdaType = lambdaType,
        parent = parent,
    ) { lambda ->
        val superCall = irCall(superFunction) {
            dispatchReceiver = irGet(function.dispatchReceiverParameter!!)
            superQualifierSymbol = superFunction.parentAsClass.symbol
            repeat(superFunction.valueParameters.size) { index ->
                putValueArgument(
                    index = index,
                    valueArgument = irCall(anyList.getSimpleFunction("get")!!) {
                        dispatchReceiver = irGet(lambda.valueParameters[0])
                        putValueArgument(0, irInt(index))
                    }
                )
            }
        }
        +irReturn(superCall)
    }
}
