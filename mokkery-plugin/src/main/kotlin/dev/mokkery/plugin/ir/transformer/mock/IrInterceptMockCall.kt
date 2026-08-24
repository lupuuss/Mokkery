package dev.mokkery.plugin.ir.transformer.mock

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.indexIfParameterOrNull
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irVararg
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.mokkeryFunctionId
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irLong
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.utils.memoryOptimizedMap

context(scope: TransformerScope)
fun IrBlockBodyBuilder.irInterceptMockMemberCall(
    function: IrSimpleFunction,
): IrCall = irInterceptMockCall(
    mokkeryInstance = { irGet(function.parameters[0]) },
    functionId = function.mokkeryFunctionId,
    function = function
)

context(scope: TransformerScope)
fun IrBlockBodyBuilder.irInterceptMockCall(
    mokkeryInstance: () -> IrExpression,
    functionId: Long,
    function: IrSimpleFunction,
): IrCall {
    val interceptFun = when {
        function.isSuspend -> MokkeryIr.Function.interceptCallSuspend
        else -> MokkeryIr.Function.interceptCall
    }
    return irCall(referenced(interceptFun)) {
        arguments[0] = mokkeryInstance()
        arguments[1] = irLong(functionId)
        arguments[2] = irVararg(
            irBuiltIns.anyNType,
            function.nonDispatchParameters.memoryOptimizedMap { irGet(it) }
        )
    }
}

context(scope: TransformerScope)
fun IrBuilderWithScope.irCallCreateFunction(
    mokkeryInstance: () -> IrExpression,
    typeParamsContainer: IrTypeParametersContainer,
    function: IrSimpleFunction,
    functionId: IrExpression,
): IrExpression {
    val parameterFun = referenced(MokkeryIr.Function.createFunctionParameter)
    return irCall(referenced(MokkeryIr.Function.createFunction)) {
        arguments[0] = functionId
        arguments[1] = irString(function.name.asString())
        arguments[2] = irCallListOf(
            type = parameterFun.returnType,
            elements = function.nonDispatchParameters.memoryOptimizedMap { param ->
                irCall(parameterFun) {
                    arguments[0] = irString(param.name.asString())
                    arguments[1] = kClassWithTypeSubstitution(mokkeryInstance, typeParamsContainer, param.type)
                    arguments[2] = irBoolean(param.isVararg)
                }
            }
        )
        arguments[3] = kClassWithTypeSubstitution(mokkeryInstance, typeParamsContainer, function.returnType)
    }
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.kClassWithTypeSubstitution(
    mokkeryInstance: () -> IrExpression,
    typeParamsContainer: IrTypeParametersContainer,
    type: IrType
): IrExpression = type
    .indexIfParameterOrNull(typeParamsContainer)
    ?.let { index ->
        irCall(referenced(MokkeryIr.Function.typeArgumentAt)) {
            arguments[0] = mokkeryInstance()
            arguments[1] = irInt(index)
        }
    } ?: kClassReference(type.eraseTypeParameters())
