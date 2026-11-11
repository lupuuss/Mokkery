package dev.mokkery.plugin.ir.transformer.templating.phases

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.transformer.CoreTransformer
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.argumentTypes
import dev.mokkery.plugin.ir.collectReturns
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.isAnyArray
import dev.mokkery.plugin.ir.transformer.templating.matcher.MatcherKind
import dev.mokkery.plugin.ir.transformer.templating.matcher.isArgMatcherType
import dev.mokkery.plugin.ir.transformer.templating.matcher.isMatcherParameter
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherFunctionKind
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherKind
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrReturn
import org.jetbrains.kotlin.ir.expressions.IrReturnableBlock
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.ir.util.transformInPlace
import org.jetbrains.kotlin.ir.util.typeSubstitutionMap

/**
 * Makes every value in a matcher position a matcher: plain values are wrapped with `eq`,
 * a single matcher spread into a vararg is wrapped with `spread`, and a value in a position of an array of matchers
 * is wrapped with `spreadArray`.
 *
 * Works on source types and never compiles callees.
 * Assumes `ctx` and `ext` are already inlined.
 */
class MatcherNormalizingTransformer(
    pluginScope: IrMokkeryPluginScope,
    private val matcherScope: IrValueParameter,
    private val matcherFunction: IrFunction?,
) : CoreTransformer(pluginScope) {

    private val eqFunction = referenced(MokkeryIr.Function.eq)
    private val spreadFunction = referenced(MokkeryIr.Function.spread)
    private val spreadArrayFunction = referenced(MokkeryIr.Function.spreadArray)

    override fun visitValueParameterNew(declaration: IrValueParameter) = declaration.transformPostfix {
        if (matcherKind() == null) return@transformPostfix
        val defaultValue = defaultValue ?: return@transformPostfix
        defaultValue.expression = when {
            isVararg -> defaultValue.expression.asMatchersArray(type)
            else -> defaultValue.expression.asMatcher(type.matcherValueType())
        }
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val function = expression.symbol.owner
        expression.transformChildrenVoid()
        if (function.matcherFunctionKind() == null) return expression
        val substitution = expression.typeSubstitutionMap
        function.parameters.forEach { param ->
            if (!param.isMatcherParameter()) return@forEach
            val arg = expression.arguments[param] ?: return@forEach
            expression.arguments[param] = when (arg) {
                is IrVararg -> arg.apply { normalizeElements() }
                else -> arg.asMatcher(param.type.matcherValueType().substitute(substitution))
            }
        }
        return expression
    }

    override fun visitVararg(expression: IrVararg) = expression.transformPostfix {
        if (elements.any { it.matcherKind() != null }) normalizeElements()
    }

    override fun visitWhen(expression: IrWhen) = expression.transformPostfix {
        when (matcherKind()) {
            MatcherKind.Array -> branches.forEach { it.result = it.result.asMatchersArray(type) }
            MatcherKind.Single -> branches.forEach { it.result = it.result.asMatcher(type) }
            null -> Unit
        }
    }

    override fun visitReturnableBlock(expression: IrReturnableBlock) = expression.transformPostfix {
        if (matcherKind() != MatcherKind.Single) return@transformPostfix
        collectReturns().forEach { it.value = it.value.asMatcher(type) }
    }

    override fun visitSetValue(expression: IrSetValue) = expression.transformPostfix {
        value = when (symbol.owner.matcherKind()) {
            MatcherKind.Single -> value.asMatcher(symbol.owner.type)
            MatcherKind.Array -> value.asMatchersArray(symbol.owner.type)
            null -> value
        }
    }

    override fun visitReturn(expression: IrReturn) = expression.transformPostfix {
        val function = matcherFunction ?: return@transformPostfix
        if (returnTargetSymbol != function.symbol) return@transformPostfix
        value = value.asMatcher(function.returnType.matcherValueType())
    }

    private fun IrVararg.normalizeElements() {
        val elementType = varargElementType
        val arrayType = type
        elements.transformInPlace { element ->
            when (element) {
                is IrSpreadElement -> element.normalize(elementType, arrayType)
                is IrExpression -> element.asMatcher(elementType)
                else -> element
            }
        }
    }

    private fun IrSpreadElement.normalize(elementType: IrType, arrayType: IrType): IrVarargElement = when (expression.matcherKind()) {
        MatcherKind.Single -> spread(expression, elementType)
        MatcherKind.Array -> this
        null -> apply { expression = expression.asMatchersArray(arrayType) }
    }

    private fun IrExpression.asMatcher(type: IrType): IrExpression = when (matcherKind()) {
        MatcherKind.Single -> this
        MatcherKind.Array -> error("Vararg matcher passed without spread!")
        null -> if (this.type.isNothing()) this else eq(this, type)
    }

    private fun IrExpression.asMatchersArray(arrayType: IrType): IrExpression {
        if (matcherKind() == MatcherKind.Array || type.isNothing()) return this
        val positionType = arrayType.takeIf { it.isAnyArray() } ?: type
        return spreadArray(asMatcher(positionType), positionType)
    }

    private fun eq(value: IrExpression, type: IrType) = value.replaceDeclarationIrBuilder {
        irCall(eqFunction, type) {
            typeArguments[0] = type
            arguments[0] = irGet(matcherScope)
            arguments[1] = value
        }
    }

    private fun spread(matcher: IrExpression, elementType: IrType) = matcher.replaceDeclarationIrBuilder {
        irCall(spreadFunction, elementType) {
            typeArguments[0] = elementType
            arguments[0] = irGet(matcherScope)
            arguments[1] = matcher
        }
    }

    private fun spreadArray(matcher: IrExpression, arrayType: IrType) = matcher.replaceDeclarationIrBuilder {
        irCall(spreadArrayFunction, arrayType) {
            typeArguments[0] = arrayType
            arguments[0] = irGet(matcherScope)
            arguments[1] = matcher
        }
    }

    private fun IrType.matcherValueType(): IrType = when {
        isArgMatcherType() -> argumentTypes.single()!!
        else -> this
    }
}
