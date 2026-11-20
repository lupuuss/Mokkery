package dev.mokkery.plugin.ir.transformer.templating

import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.ir.KotlinIr
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.defaultTypeErased
import dev.mokkery.plugin.ir.hasNonDispatchParameters
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.irLambdaOf
import dev.mokkery.plugin.ir.kClassReference
import dev.mokkery.plugin.ir.mokkeryFunctionId
import org.jetbrains.kotlin.ir.builders.IrBlockBodyBuilder
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irLong
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.builders.irString
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.parentAsClass

context(scope: TransformerScope)
fun IrBuilderWithScope.irTemplate(
    templatingScope: IrValueParameter,
    mock: IrExpression,
    function: IrSimpleFunction,
    resultType: IrType,
    matchers: IrBlockBodyBuilder.(IrSimpleFunction) -> IrExpression,
    original: (IrBlockBodyBuilder.(IrSimpleFunction) -> IrExpression)?,
): IrCall {
    val templateFun = when (original) {
        null -> referenced(MokkeryIr.Function.requireTemplate)
        else -> referenced(MokkeryIr.Function.tryTemplate)
    }
    fun parameter(name: String) = templateFun.parameters.single { it.name.asString() == name }
    return irCall(templateFun, referenced(MokkeryIr.Class.RunTemplateResult).typeWith(resultType)) {
        if (original != null) typeArguments[0] = resultType
        arguments[0] = irGet(templatingScope)
        arguments[parameter("mock")] = mock
        arguments[parameter("functionId")] = irLong(function.mokkeryFunctionId)
        arguments[parameter("functionName")] = irString(function.name.asString())
        val matchersParam = parameter("matchers")
        arguments[matchersParam] = irLambdaOf(matchersParam.type) {
            +irReturn(if (function.hasNonDispatchParameters()) matchers(it) else irEmptyMatchers())
        }
        when (original) {
            null -> arguments[parameter("mockedType")] = kClassReference(function.parentAsClass.defaultTypeErased)
            else -> arguments[parameter("original")] = irLambdaOf(irBuiltIns.functionN(0).typeWith(resultType)) {
                +irReturn(original(it))
            }
        }
    }
}

context(scope: TransformerScope)
private fun IrBuilderWithScope.irEmptyMatchers(): IrCall {
    val matcherType = referenced(MokkeryIr.Class.ArgMatcher).typeWith(irBuiltIns.anyNType)
    return irCall(referenced(KotlinIr.Function.emptyList), irBuiltIns.listClass.typeWith(matcherType)) {
        typeArguments[0] = matcherType
    }
}
