package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.diagnostics.MatcherProcessingResult.RegularExpression
import dev.mokkery.plugin.fir.acceptsMatcher
import dev.mokkery.plugin.fir.isCompositeMatcher
import dev.mokkery.plugin.fir.isMatcher
import dev.mokkery.plugin.fir.isVarargMatcher
import dev.mokkery.plugin.fir.unwrapExpressionOrArgument
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.FirVariable
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.lastExpression
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol

class MatchersProcessor(private val session: FirSession) {

    private val matchers = mutableMapOf<FirBasedSymbol<*>, MatcherProcessingResult>()

    fun processVariable(variable: FirVariable): MatcherProcessingResult = matchers.getOrPut(variable.symbol) {
        when {
            variable.symbol.acceptsMatcher(session) -> MatcherProcessingResult.MatcherExpression(
                matcherType(isComposite = false, isVararg = false)
            )
            variable.initializer != null -> extractMatcherSingleType(variable.initializer)
                ?.let { MatcherProcessingResult.MatcherExpression(it) }
                ?: RegularExpression
            else -> RegularExpression
        }
    }

    fun extractMatcherTypes(expression: FirExpression?): Sequence<FirMatcherType> = sequence {
        expression ?: return@sequence
        val unwrappedExpression = expression.unwrapExpressionOrArgument()
        when (unwrappedExpression) {
            is FirFunctionCall -> matcherTypeOrNullOf(unwrappedExpression)?.let { yield(it) }
            is FirQualifiedAccessExpression -> unwrappedExpression.calleeReference
                .toResolvedCallableSymbol()
                ?.let(matchers::get)
                ?.let { it as? MatcherProcessingResult.MatcherExpression }
                ?.let { yield(it.matcherType) }
            is FirWhenExpression -> unwrappedExpression.branches.forEach { branch ->
                yieldAll(
                    extractMatcherTypes(
                        branch
                            .result
                            .lastExpression
                    )
                )
            }
            is FirVarargArgumentsExpression -> unwrappedExpression
                .arguments
                .forEach { yieldAll(extractMatcherTypes(it)) }
            else -> false
        }
    }

    fun extractMatcherSingleType(expression: FirExpression?): FirMatcherType? {
        val processed = extractMatcherTypes(expression).toList()
        return when {
            processed.isEmpty() -> null
            else -> matcherType(
                isComposite = processed.all { it.isComposite },
                isVararg = processed.all { it.isVararg }
            )
        }
    }

    fun getResultFor(symbol: FirBasedSymbol<*>): MatcherProcessingResult? = matchers[symbol]

    private fun matcherTypeOrNullOf(call: FirFunctionCall): FirMatcherType? {
        val callee = call.calleeReference as? FirResolvedNamedReference ?: return null
        val symbol = callee.resolvedSymbol as? FirNamedFunctionSymbol ?: return null
        if (!symbol.isMatcher()) return null
        return object : FirMatcherType {
            override val isComposite = symbol.isCompositeMatcher(session)
            override val isVararg = when {
                isComposite -> call.arguments
                    .any { arg -> extractMatcherTypes(arg).any { it.isVararg } }
                else -> symbol.isVarargMatcher(session)
            }
            override val isRegular = !isComposite && !isVararg
        }
    }

    private fun matcherType(isComposite: Boolean, isVararg: Boolean): FirMatcherType {
        return object : FirMatcherType {
            override val isComposite = isComposite
            override val isVararg = isVararg
            override val isRegular = !isComposite && !isVararg
        }
    }
}

fun MatchersProcessor.usesMatchers(expression: FirExpression?): Boolean = extractMatcherTypes(expression).any()

fun MatchersProcessor.usesVarargMatchers(expression: FirExpression?): Boolean = extractMatcherTypes(expression).any {
    it.isVararg
}

interface MatcherProcessingResult {

    interface RegularExpression : MatcherProcessingResult {

        companion object : RegularExpression
    }

    interface MatcherExpression : MatcherProcessingResult {
        val matcherType: FirMatcherType

        companion object {
            operator fun invoke(type: FirMatcherType): MatcherExpression = object : MatcherExpression {
                override val matcherType = type
            }
        }
    }
}

interface FirMatcherType {
    val isComposite: Boolean
    val isRegular: Boolean
    val isVararg: Boolean
}
