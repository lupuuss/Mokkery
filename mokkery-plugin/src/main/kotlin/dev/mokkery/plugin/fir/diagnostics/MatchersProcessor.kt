package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.fir.isMatcher
import dev.mokkery.plugin.fir.unwrapExpressionOrArgument
import org.jetbrains.kotlin.fir.declarations.FirVariable
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.lastExpression
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol

class MatchersProcessor(matcherParameters: List<FirBasedSymbol<*>>) {

    private val matchers = matcherParameters.associateWithTo(mutableMapOf<FirBasedSymbol<*>, Boolean>()) { true }

    fun processVariable(variable: FirVariable) {
        matchers.getOrPut(variable.symbol) { isMatcher(variable.initializer) }
    }

    fun isMatcherVariable(symbol: FirBasedSymbol<*>): Boolean? = matchers[symbol]

    fun isMatcher(expression: FirExpression?): Boolean {
        expression ?: return false
        return when (val unwrappedExpression = expression.unwrapExpressionOrArgument()) {
            is FirFunctionCall -> isMatcher(unwrappedExpression)
            is FirQualifiedAccessExpression -> unwrappedExpression.calleeReference
                .toResolvedCallableSymbol()
                ?.let(matchers::get) == true
            is FirWhenExpression -> unwrappedExpression.branches.any { isMatcher(it.result.lastExpression) }
            is FirVarargArgumentsExpression -> unwrappedExpression.arguments.any { isMatcher(it) }
            else -> false
        }
    }

    fun isMatcher(call: FirFunctionCall): Boolean {
        val callee = call.calleeReference as? FirResolvedNamedReference ?: return false
        val symbol = callee.resolvedSymbol as? FirNamedFunctionSymbol ?: return false
        return symbol.isMatcher() && !symbol.isMaterializer()
    }
}

fun FirFunctionSymbol<*>.isMaterializer(): Boolean = callableId == Mokkery.Callable.materialize
