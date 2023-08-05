package dev.mokkery.plugin.transformers

import dev.mokkery.plugin.core.Kotlin
import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.TransformerScope
import dev.mokkery.plugin.core.getClass
import dev.mokkery.plugin.ext.kClassReferenceUnified
import dev.mokkery.plugin.ext.nonGenericReturnTypeOrAny
import org.jetbrains.kotlin.backend.jvm.fullValueParameterList
import org.jetbrains.kotlin.backend.jvm.functionByName
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.builders.irVararg
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.isVararg
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
    val interceptorClass = transformer.getClass(Mokkery.Class.MokkeryInterceptor)
    val interceptorScopeClass = transformer.getClass(Mokkery.Class.MokkeryInterceptorScope)
    val callContextClass = transformer.getClass(Mokkery.Class.CallContext)
    val pluginContext = transformer.pluginContext
    val mokkeryCall = if (function.isSuspend) {
        irCall(interceptorClass.symbol.functionByName("interceptSuspendCall"))
    } else {
        irCall(interceptorClass.symbol.functionByName("interceptCall"))
    }
    mokkeryCall.dispatchReceiver = interceptorScopeClass
        .getPropertyGetter("interceptor")!!
        .let(::irCall)
        .apply {
            dispatchReceiver = mokkeryScope
        }
    val contextCreationCall = irCall(callContextClass.primaryConstructor!!).apply {
        putValueArgument(0, mokkeryScope)
        putValueArgument(1, irString(function.name.asString()))
        putValueArgument(2, kClassReferenceUnified(pluginContext, function.nonGenericReturnTypeOrAny(pluginContext)))
        putValueArgument(3, irCallArgsList(transformer, function.fullValueParameterList))
    }
    mokkeryCall.putValueArgument(0, contextCreationCall)
    return mokkeryCall
}

private fun IrBuilderWithScope.irCallArgsList(scope: TransformerScope, parameters: List<IrValueParameter>): IrCall {
    val callArgClass = scope.getClass(Mokkery.Class.CallArg)
    val pluginContext = scope.pluginContext
    val args = irVararg(
        elementType = callArgClass.defaultType,
        values = parameters
            .map {
                irCall(callArgClass.primaryConstructor!!).apply {
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
    val listOf = pluginContext.referenceFunctions(Kotlin.FunctionId.listOf).first {
        it.owner.valueParameters.firstOrNull()?.isVararg == true
    }
    return irCall(listOf).apply {
        putValueArgument(0, args)
    }
}

