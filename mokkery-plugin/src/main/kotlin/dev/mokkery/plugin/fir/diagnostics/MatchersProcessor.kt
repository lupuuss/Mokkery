package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.fir.hasMatcherScope
import dev.mokkery.plugin.fir.unwrapExpressionOrArgument
import org.jetbrains.kotlin.fir.declarations.FirVariable
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.expressions.FirElvisExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.lastExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol

class MatchersProcessor(matcherParameters: List<FirBasedSymbol<*>>) {

    private val matchers = matcherParameters.associateWithTo(mutableMapOf<FirBasedSymbol<*>, Boolean>()) { true }

    fun processVariable(variable: FirVariable) = processValue(variable.symbol, variable.initializer)

    fun processValue(symbol: FirBasedSymbol<*>, value: FirExpression?) {
        matchers.getOrPut(symbol) { isMatcher(value) }
    }

    fun isMatcherVariable(symbol: FirBasedSymbol<*>): Boolean? = matchers[symbol]

    fun isMatcher(expression: FirExpression?): Boolean {
        expression ?: return false
        return when (val unwrappedExpression = expression.unwrapExpressionOrArgument()) {
            is FirCallableReferenceAccess -> false
            is FirThisReceiverExpression -> unwrappedExpression.calleeReference.boundSymbol?.let(matchers::get) == true
            is FirQualifiedAccessExpression -> {
                val symbol = unwrappedExpression.calleeReference.toResolvedCallableSymbol()
                symbol != null && (matchers[symbol] == true || symbol.isMatcherBuilder())
            }
            is FirElvisExpression -> isMatcher(unwrappedExpression.rhs)
            is FirWhenExpression -> unwrappedExpression.branches.any { isMatcher(it.result.lastExpression) }
            is FirVarargArgumentsExpression -> unwrappedExpression.arguments.any { isMatcher(it) }
            else -> false
        }
    }
}

fun FirCallableSymbol<*>.isMaterializer(): Boolean = callableId == Mokkery.Callable.materialize

fun FirCallableSymbol<*>.isMatcherBuilder(): Boolean = isMatcherLikeDeclaration() && !isMaterializer()

/**
 * Regular matcher builders + materializers
 */
fun FirCallableSymbol<*>.isMatcherLikeDeclaration(): Boolean = invokedFunction()?.hasMatcherScope() == true

private fun FirCallableSymbol<*>.invokedFunction(): FirFunctionSymbol<*>? = when (this) {
    is FirNamedFunctionSymbol -> this
    is FirPropertySymbol -> getterSymbol
    else -> null
}
