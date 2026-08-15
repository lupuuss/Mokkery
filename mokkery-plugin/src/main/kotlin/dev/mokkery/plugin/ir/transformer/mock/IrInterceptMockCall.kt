package dev.mokkery.plugin.ir.transformer.mock

import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.referencedGetter
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.indexIfParameterOrNull
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irCallConstructor
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.transformer.core.irCallListOf
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBoolean
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrTypeParametersContainer
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrFail
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.eraseTypeParameters
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.nonDispatchParameters
import org.jetbrains.kotlin.ir.util.parentAsClass

context(scope: TransformerScope)
fun IrBlockBodyBuilder.irInterceptMockMemberCall(
    function: IrSimpleFunction,
    functionId: Int,
): IrCall = irInterceptMockCall(
    mokkeryInstance = { irGet(function.parameters[0]) },
    typeParamsContainer = function.parentAsClass,
    function = function,
    functionId = functionId
)

context(scope: TransformerScope)
fun IrBlockBodyBuilder.irInterceptMockCall(
    mokkeryInstance: () -> IrExpression,
    typeParamsContainer: IrTypeParametersContainer,
    function: IrSimpleFunction,
    functionId: Int,
): IrCall {
    val interceptorProperty = referencedGetter(MokkeryIr.Property.callInterceptor)
    val interceptorClass = interceptorProperty.returnType.classOrFail
    val interceptFun = interceptorClass
        .functions
        .first { it.owner.name.asString() == "intercept" && it.owner.isSuspend == function.isSuspend }
    return irCall(interceptFun) {
        arguments[0] = interceptorProperty
            .let(::irCall)
            .apply { arguments[0] = mokkeryInstance() }
        val scopeCreationFun = when {
            function.isSuspend -> MokkeryIr.Function.suspendCallScope
            else -> MokkeryIr.Function.blockingCallScope
        }
        val scopeCreationCall = irCall(referenced(scopeCreationFun)) {
            arguments[0] = mokkeryInstance()
            arguments[1] = irString(function.name.asString())
            arguments[2] = kClassWithTypeSubstitution(
                mokkeryInstance = mokkeryInstance,
                typeParamsContainer = typeParamsContainer,
                type = function.returnType
            )
            arguments[3] = irCallArgsList(mokkeryInstance, function, typeParamsContainer)
            arguments[4] = irInt(functionId)
        }
        arguments[1] = scopeCreationCall
    }
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irCallArgsList(
    mokkeryInstance: () -> IrExpression,
    function: IrSimpleFunction,
    paramsContainer: IrTypeParametersContainer
): IrCall {
    val callArgClass = referenced(MokkeryIr.Class.CallArgument)
    val callArgs = function
        .nonDispatchParameters
        .map { param ->
            val constructor = callArgClass.constructors.single { it.parameters.size == 4 }
            irCallConstructor(constructor) {
                arguments[0] = irGet(param)
                arguments[1] = irString(param.name.asString())
                arguments[2] = kClassWithTypeSubstitution(
                    mokkeryInstance = mokkeryInstance,
                    typeParamsContainer = paramsContainer,
                    type = param.type
                )
                arguments[3] = irBoolean(param.isVararg)
            }
        }
    return irCallListOf(callArgClass.defaultType, callArgs)
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
