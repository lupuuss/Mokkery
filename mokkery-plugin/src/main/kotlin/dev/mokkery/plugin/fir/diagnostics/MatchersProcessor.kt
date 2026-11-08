package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.fir.hasMatcherScope
import dev.mokkery.plugin.fir.unwrapExpressionOrArgument
import org.jetbrains.kotlin.fir.declarations.FirVariable
import org.jetbrains.kotlin.fir.expressions.FirElvisExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.lastExpression
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.references.toResolvedNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol

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
            is FirElvisExpression -> isMatcher(unwrappedExpression.rhs)
            is FirWhenExpression -> unwrappedExpression.branches.any { isMatcher(it.result.lastExpression) }
            is FirVarargArgumentsExpression -> unwrappedExpression.arguments.any { isMatcher(it) }
            else -> false
        }
    }

    fun isMatcher(call: FirFunctionCall): Boolean {
        val symbol = call.calleeReference.toResolvedNamedFunctionSymbol() ?: return false
        return symbol.isMatcherBuilder()
    }
}

fun FirFunctionSymbol<*>.isMaterializer(): Boolean = callableId == Mokkery.Callable.materialize

fun FirFunctionSymbol<*>.isMatcherBuilder(): Boolean = hasMatcherScope() && !isMaterializer()

fun FirCallableSymbol<*>.isMatcherDeclaration(): Boolean = (this as? FirFunctionSymbol<*>)?.hasMatcherScope() == true
