package dev.mokkery.plugin.ir.transformer.templating.phases

import dev.mokkery.plugin.core.ir.IrMokkeryPluginScope
import dev.mokkery.plugin.core.ir.irBuiltIns
import dev.mokkery.plugin.core.ir.transformer.CoreTransformer
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.collectReturns
import dev.mokkery.plugin.ir.transformer.templating.matcher.MatcherFunctionKind
import dev.mokkery.plugin.ir.transformer.templating.matcher.MatcherKind
import dev.mokkery.plugin.ir.transformer.templating.matcher.isMatcherParameter
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherFunctionKind
import dev.mokkery.plugin.ir.transformer.templating.matcher.matcherKind
import dev.mokkery.plugin.ir.transformer.templating.runMatcherPhases
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
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
import org.jetbrains.kotlin.ir.util.getArrayElementType
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.substitute
import org.jetbrains.kotlin.ir.util.typeSubstitutionMap

/**
 * Retypes matchers from their source types to `ArgMatcher`: the signature of [matcherFunction] first, then its body.
 * Called matcher builders are compiled to get their signatures.
 *
 * Assumes [MatcherNormalizingTransformer] ran, so every matcher position already holds a matcher.
 */
class MatcherRetypingTransformer(
    pluginScope: IrMokkeryPluginScope,
    private val matcherFunction: IrFunction?,
) : CoreTransformer(pluginScope) {

    private val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        if (declaration == matcherFunction) declaration.retypeSignature()
        return super.visitFunctionNew(declaration)
    }

    override fun visitCall(expression: IrCall): IrExpression {
        val function = runMatcherPhases(expression.symbol.owner)
        expression.transformChildrenVoid()
        when (function.matcherFunctionKind()) {
            MatcherFunctionKind.Matches -> expression.type = argMatcherClass.typeWith(expression.type)
            MatcherFunctionKind.SpreadArray -> expression.type = expression.type.asMatchersArrayType()
            MatcherFunctionKind.Builder -> expression.retypeAsCompiledMatcherCall(function)
            MatcherFunctionKind.Materializer, MatcherFunctionKind.MaterializerVararg, null -> Unit
        }
        return expression
    }

    override fun visitVariable(declaration: IrVariable) = declaration.transformPostfix {
        if (matcherKind() != null) type = initializer!!.type
    }

    override fun visitVararg(expression: IrVararg) = expression.transformPostfix {
        if (elements.none { it.matcherKind() != null }) return@transformPostfix
        retype(argMatcherClass.typeWith(varargElementType))
    }

    override fun visitReturnableBlock(expression: IrReturnableBlock) = expression.transformPostfix {
        if (matcherKind() != MatcherKind.Single) return@transformPostfix
        val targetType = argMatcherClass.typeWith(type)
        type = targetType
        collectReturns().forEach { it.type = targetType }
    }

    override fun visitBlock(expression: IrBlock) = expression.transformPostfix {
        if (matcherKind() == null) return@transformPostfix
        type = (statements.last() as IrExpression).type
    }

    override fun visitWhen(expression: IrWhen) = expression.transformPostfix {
        type = when (matcherKind()) {
            MatcherKind.Array -> branches.first { it.result.matcherKind() == MatcherKind.Array }.result.type
            MatcherKind.Single -> argMatcherClass.typeWith(type)
            null -> return@transformPostfix
        }
    }

    override fun visitGetValue(expression: IrGetValue) = expression.transformPostfix {
        if (symbol.owner.matcherKind() != null) type = symbol.owner.type
    }

    private fun IrCall.retypeAsCompiledMatcherCall(function: IrSimpleFunction) {
        val substitution = typeSubstitutionMap
        type = function.returnType.substitute(substitution)
        function.parameters.forEach { param ->
            val arg = arguments[param] as? IrVararg ?: return@forEach
            if (param.isMatcherParameter()) arg.retype(param.varargElementType!!.substitute(substitution))
        }
    }

    private fun IrVararg.retype(elementType: IrType) {
        varargElementType = elementType
        type = irBuiltIns.arrayClass.typeWith(elementType)
    }

    private fun IrFunction.retypeSignature() {
        parameters.forEach {
            if (!it.isMatcherParameter()) return@forEach
            if (it.isVararg) {
                val matcherType = argMatcherClass.typeWith(it.type.getArrayElementType(irBuiltIns))
                it.type = irBuiltIns.arrayClass.typeWith(matcherType)
                it.varargElementType = matcherType
            } else {
                it.type = argMatcherClass.typeWith(it.type)
            }
        }
        returnType = argMatcherClass.typeWith(returnType)
    }

    private fun IrType.asMatchersArrayType(): IrType = irBuiltIns
        .arrayClass
        .typeWith(argMatcherClass.typeWith(getArrayElementType(irBuiltIns)))
}
