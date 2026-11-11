package dev.mokkery.plugin.ir.transformer.templating.matcher

import dev.mokkery.plugin.core.ir.transformer.TransformerScope
import dev.mokkery.plugin.core.ir.transformer.referenced
import dev.mokkery.plugin.ir.MokkeryIr
import dev.mokkery.plugin.ir.collectReturns
import dev.mokkery.plugin.ir.isAnyArray
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
import org.jetbrains.kotlin.ir.expressions.IrVarargElement
import org.jetbrains.kotlin.ir.expressions.IrWhen
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.isMarkedNullable
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.util.isSubclassOf
import org.jetbrains.kotlin.ir.util.isVararg

enum class MatcherKind { Single, Array }

context(scope: TransformerScope)
fun IrValueDeclaration.matcherKind(): MatcherKind? = when (this) {
    is IrVariable -> initializer?.matcherKind()
    is IrValueParameter -> when {
        !isMatcherParameter() || (parent as? IrFunction)?.matcherFunctionKind() == null -> null
        isVararg -> MatcherKind.Array
        else -> MatcherKind.Single
    }
    else -> null
}

context(scope: TransformerScope)
fun IrVarargElement.matcherKind(): MatcherKind? = when (this) {
    is IrSpreadElement -> expression.matcherKind()
    is IrCall -> when (symbol.owner.matcherFunctionKind()) {
        MatcherFunctionKind.Builder, MatcherFunctionKind.Matches -> MatcherKind.Single
        MatcherFunctionKind.SpreadArray -> MatcherKind.Array
        MatcherFunctionKind.Materializer, MatcherFunctionKind.MaterializerVararg, null -> null
    }
    is IrGetValue -> symbol.owner.matcherKind()
    is IrReturnableBlock -> MatcherKind.Single.takeIf {
        collectReturns().any { it.value.matcherKind() == MatcherKind.Single }
    }
    is IrBlock -> (statements.lastOrNull() as? IrExpression)?.matcherKind()
    is IrWhen -> whenMatcherKind()
    else -> null
}

context(scope: TransformerScope)
private fun IrWhen.whenMatcherKind(): MatcherKind? {
    val kinds = branches.map { it.result.matcherKind() }
    return when {
        MatcherKind.Array in kinds -> MatcherKind.Array.takeIf {
            branches.all { it.result.type.isAnyArray() || it.result.type.isNothing() }
        }
        MatcherKind.Single in kinds -> MatcherKind.Single
        else -> null
    }
}

context(scope: TransformerScope)
fun IrType.isMatcher(): Boolean = !isMarkedNullable() && isArgMatcherType()

context(scope: TransformerScope)
fun IrType.isArgMatcherType(): Boolean {
    val argMatcherClass = referenced(MokkeryIr.Class.ArgMatcher)
    val clazz = classOrNull?.owner ?: return false
    return clazz == argMatcherClass || clazz.isSubclassOf(argMatcherClass)
}
