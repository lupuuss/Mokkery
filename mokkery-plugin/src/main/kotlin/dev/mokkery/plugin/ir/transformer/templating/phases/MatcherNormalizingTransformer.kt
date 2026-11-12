package dev.mokkery.plugin.ir.transformer.templating.phases

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.core.ir.transformer.replaceDeclarationIrBuilder
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.collectReturns
import dev.mokkery.plugin.ir.irCall
import dev.mokkery.plugin.ir.isAnyArray
import dev.mokkery.plugin.ir.transformer.templating.matcher.MatcherArity
import dev.mokkery.plugin.ir.transformer.templating.matcher.isMatcherParameter
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherArity
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherFunctionKind
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
 * Works on source types: signatures of matcher builders are retyped only after the whole module is compiled.
 * Assumes `ctx` and `ext` are already inlined.
 */
class MatcherNormalizingTransformer(
    pluginScope: IrMokkeryPluginScope,
    private val matcherScope: IrValueParameter,
    private val matcherFunction: IrFunction?,
) : PhaseTransformer(pluginScope) {

    private val eqFunction = referenced(MokkeryIr.Function.eq)
    private val spreadFunction = referenced(MokkeryIr.Function.spread)
    private val spreadArrayFunction = referenced(MokkeryIr.Function.spreadArray)

    override fun visitValueParameterNew(declaration: IrValueParameter) = declaration.transformPostfix {
        if (matcherArity() == null) return@transformPostfix
        val defaultValue = defaultValue ?: return@transformPostfix
        defaultValue.expression = when {
            isVararg -> defaultValue.expression.asMatchersArray(type)
            else -> defaultValue.expression.asMatcher(type)
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
            val type = param.type.substitute(substitution)
            expression.arguments[param] = when {
                arg is IrVararg -> arg.apply { normalizeElements() }
                param.isVararg -> arg.asMatchersArray(type)
                else -> arg.asMatcher(type)
            }
        }
        return expression
    }

    override fun visitVararg(expression: IrVararg) = expression.transformPostfix {
        if (elements.any { it.matcherArity() != null }) normalizeElements()
    }

    override fun visitWhen(expression: IrWhen) = expression.transformPostfix {
        when (matcherArity()) {
            MatcherArity.Array -> branches.forEach { it.result = it.result.asMatchersArray(type) }
            MatcherArity.Single -> branches.forEach { it.result = it.result.asMatcher(type) }
            null -> Unit
        }
    }

    override fun visitReturnableBlock(expression: IrReturnableBlock) = expression.transformPostfix {
        if (matcherArity() != MatcherArity.Single) return@transformPostfix
        collectReturns().forEach { it.value = it.value.asMatcher(type) }
    }

    override fun visitSetValue(expression: IrSetValue) = expression.transformPostfix {
        value = when (symbol.owner.matcherArity()) {
            MatcherArity.Single -> value.asMatcher(symbol.owner.type)
            MatcherArity.Array -> value.asMatchersArray(symbol.owner.type)
            null -> value
        }
    }

    override fun visitReturn(expression: IrReturn) = expression.transformPostfix {
        val function = matcherFunction ?: return@transformPostfix
        if (returnTargetSymbol != function.symbol) return@transformPostfix
        value = value.asMatcher(function.returnType)
    }

    private fun IrVararg.normalizeElements() {
        val elementType = varargElementType
        elements.transformInPlace { element ->
            when (element) {
                is IrSpreadElement -> element.normalize(elementType)
                is IrExpression -> element.asMatcher(elementType)
                else -> element
            }
        }
    }

    private fun IrSpreadElement.normalize(elementType: IrType): IrVarargElement = when (expression.matcherArity()) {
        MatcherArity.Single -> spread(expression, elementType)
        MatcherArity.Array -> this
        null -> apply { expression = expression.asMatchersArray(expression.type) }
    }

    private fun IrExpression.asMatcher(type: IrType): IrExpression = when (matcherArity()) {
        MatcherArity.Single -> this
        MatcherArity.Array -> error("Vararg matcher passed without spread!")
        null -> if (this.type.isNothing()) this else eq(this, type)
    }

    private fun IrExpression.asMatchersArray(arrayType: IrType): IrExpression {
        if (matcherArity() == MatcherArity.Array || type.isNothing()) return this
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
}
