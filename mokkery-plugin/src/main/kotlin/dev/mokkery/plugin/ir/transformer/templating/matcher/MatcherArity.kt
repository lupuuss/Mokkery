package dev.mokkery.plugin.ir.transformer.templating.matcher

import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.ir.collectReturns
import dev.mokkery.plugin.ir.isAnyArray
import org.jetbrains.kotlin.builtins.StandardNames.BUILT_INS_PACKAGE_FQ_NAME
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrBlock
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrReturnableBlock
import org.jetbrains.kotlin.ir.expressions.IrSpreadElement
import org.jetbrains.kotlin.ir.expressions.IrVararg
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.util.isVararg
import org.jetbrains.kotlin.ir.util.kotlinFqName

enum class MatcherArity { Single, Array }

context(scope: TransformerScope)
fun IrValueDeclaration.matcherArity(): MatcherArity? = when (this) {
    is IrVariable -> initializer?.matcherArity()
    is IrValueParameter -> when {
        !isMatcherParameter() || (parent as? IrFunction)?.matcherFunctionKind() == null -> null
        isVararg -> MatcherArity.Array
        else -> MatcherArity.Single
    }
    else -> null
}

context(scope: TransformerScope)
fun IrVarargElement.matcherArity(): MatcherArity? = when (this) {
    is IrSpreadElement -> expression.matcherArity()
    is IrCall -> when (symbol.owner.matcherFunctionKind()) {
        MatcherFunctionKind.Builder, MatcherFunctionKind.Matches -> MatcherArity.Single
        MatcherFunctionKind.SpreadArray -> MatcherArity.Array
        MatcherFunctionKind.Materializer, MatcherFunctionKind.MaterializerVararg -> null
        null -> MatcherArity.Array.takeIf { isMatchersArrayLiteral() }
    }
    is IrGetValue -> symbol.owner.matcherArity()
    is IrVararg -> MatcherArity.Array.takeIf { elements.any { it.matcherArity() != null } }
    is IrReturnableBlock -> MatcherArity.Single.takeIf {
        collectReturns().any { it.value.matcherArity() == MatcherArity.Single }
    }
    is IrBlock -> (statements.lastOrNull() as? IrExpression)?.matcherArity()
    is IrWhen -> {
        val arities = branches.map { it.result.matcherArity() }
        when {
            MatcherArity.Array in arities -> MatcherArity.Array.takeIf {
                branches.all { it.result.type.isAnyArray() || it.result.type.isNothing() }
            }
            MatcherArity.Single in arities -> MatcherArity.Single
            else -> null
        }
    }
    else -> null
}

context(scope: TransformerScope)
fun IrCall.isMatchersArrayLiteral(): Boolean = arrayLiteralElements()?.matcherArity() != null

fun IrCall.arrayLiteralElements(): IrVararg? {
    val function = symbol.owner
    val name = function.name.asString()
    if (function.kotlinFqName.parent() != BUILT_INS_PACKAGE_FQ_NAME) return null
    if (name != "arrayOf" && !name.endsWith("ArrayOf")) return null
    return arguments.singleOrNull() as? IrVararg
}
