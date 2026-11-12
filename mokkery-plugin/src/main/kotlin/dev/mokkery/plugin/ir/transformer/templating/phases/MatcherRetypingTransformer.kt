package dev.mokkery.plugin.ir.transformer.templating.phases

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.ir.collectReturns
import dev.mokkery.plugin.ir.transformer.templating.matcher.MatcherArity
import dev.mokkery.plugin.ir.transformer.templating.matcher.MatcherFunctionKind
import dev.mokkery.plugin.ir.transformer.templating.matcher.asMatcherType
import dev.mokkery.plugin.ir.transformer.templating.matcher.asMatchersArrayType
import dev.mokkery.plugin.ir.transformer.templating.matcher.isMatchersArrayLiteral
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherArity
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherFunctionKind
import dev.mokkery.plugin.ir.transformer.templating.signature.matcherCompiledSignature
import dev.mokkery.plugin.ir.transformer.templating.signature.matcherCompiledType
import dev.mokkery.plugin.ir.transformer.templating.signature.retypeTo
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturnableBlock
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.typeWith

/**
 * Retypes matchers from their source types to `ArgMatcher`.
 * Types of called matcher builders are computed from their source signatures, which are retyped
 * only after the whole module is compiled.
 *
 * Assumes [MatcherNormalizingTransformer] ran, so every matcher position already holds a matcher.
 */
class MatcherRetypingTransformer(pluginScope: IrMokkeryPluginScope) : PhaseTransformer(pluginScope) {

    override fun visitCall(expression: IrCall): IrExpression {
        val function = expression.symbol.owner
        expression.transformChildrenVoid()
        when (function.matcherFunctionKind()) {
            MatcherFunctionKind.Matches -> expression.type = expression.type.asMatcherType()
            MatcherFunctionKind.SpreadArray -> expression.type = expression.type.asMatchersArrayType()
            MatcherFunctionKind.Builder -> expression.retypeTo(function.matcherCompiledSignature)
            MatcherFunctionKind.Materializer, MatcherFunctionKind.MaterializerVararg -> Unit
            null -> if (expression.isMatchersArrayLiteral()) expression.type = expression.type.asMatchersArrayType()
        }
        return expression
    }

    override fun visitVariable(declaration: IrVariable) = declaration.transformPostfix {
        if (matcherArity() != null) type = initializer!!.type
    }

    override fun visitVararg(expression: IrVararg) = expression.transformPostfix {
        if (elements.none { it.matcherArity() != null }) return@transformPostfix
        retype(varargElementType.asMatcherType())
    }

    override fun visitReturnableBlock(expression: IrReturnableBlock) = expression.transformPostfix {
        if (matcherArity() != MatcherArity.Single) return@transformPostfix
        type = collectReturns().first { it.value.matcherArity() == MatcherArity.Single }.value.type
    }

    override fun visitBlock(expression: IrBlock) = expression.transformPostfix {
        if (matcherArity() == null) return@transformPostfix
        type = (statements.last() as IrExpression).type
    }

    override fun visitWhen(expression: IrWhen) = expression.transformPostfix {
        val arity = matcherArity() ?: return@transformPostfix
        type = branches.first { it.result.matcherArity() == arity }.result.type
    }

    override fun visitGetValue(expression: IrGetValue) = expression.transformPostfix {
        val owner = symbol.owner
        type = when {
            owner.matcherArity() == null -> return@transformPostfix
            owner is IrValueParameter -> owner.matcherCompiledType
            else -> owner.type
        }
    }

    private fun IrVararg.retype(elementType: IrType) {
        varargElementType = elementType
        type = irBuiltIns.arrayClass.typeWith(elementType)
    }
}
