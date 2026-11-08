package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.fir.hasMatcherScope
import dev.mokkery.plugin.fir.isSpread
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
import org.jetbrains.kotlin.fir.references.toResolvedBaseSymbol
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol

class MatchersProcessor(matcherParameters: List<FirBasedSymbol<*>>) {

    private val matchers = matcherParameters.associateWithTo(mutableMapOf<FirBasedSymbol<*>, Boolean>()) { true }
    private val matchersVarargs = matcherParameters.filterTo(mutableSetOf()) { it is FirValueParameterSymbol && it.isVararg }

    fun processVariable(variable: FirVariable) = processValue(variable.symbol, variable.initializer)

    fun processValue(symbol: FirBasedSymbol<*>, value: FirExpression?) {
        matchers.getOrPut(symbol) { isMatcher(value) }
        if (isMatchersVararg(value)) matchersVarargs += symbol
    }

    fun isMatcherVariable(symbol: FirBasedSymbol<*>): Boolean? = matchers[symbol]

    fun isMatcher(expression: FirExpression?): Boolean = expression.anyValue {
        when (it) {
            is FirCallableReferenceAccess -> false
            is FirThisReceiverExpression -> it.calleeReference.boundSymbol?.let(matchers::get) == true
            is FirQualifiedAccessExpression -> {
                val symbol = it.calleeReference.toResolvedCallableSymbol()
                symbol != null && (matchers[symbol] == true || symbol.isMatcherBuilder())
            }
            is FirVarargArgumentsExpression -> it.arguments.any(::isMatcher)
            else -> false
        }
    }

    fun isMatchersVararg(expression: FirExpression?): Boolean = expression?.isSpread() != true && expression.anyValue {
        it is FirQualifiedAccessExpression && it.calleeReference.toResolvedBaseSymbol() in matchersVarargs
    }

    private fun FirExpression?.anyValue(predicate: (FirExpression) -> Boolean): Boolean {
        return when (val unwrappedExpression = this?.unwrapExpressionOrArgument()) {
            null -> false
            is FirElvisExpression -> unwrappedExpression.rhs.anyValue(predicate)
            is FirWhenExpression -> unwrappedExpression.branches.any { it.result.lastExpression.anyValue(predicate) }
            else -> predicate(unwrappedExpression)
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
