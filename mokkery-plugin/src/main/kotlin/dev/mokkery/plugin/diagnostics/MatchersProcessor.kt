package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.diagnostics.FirMatcherType.Composite
import dev.mokkery.plugin.diagnostics.FirMatcherType.Regular
import dev.mokkery.plugin.diagnostics.MatcherProcessingResult.MatcherExpr
import dev.mokkery.plugin.diagnostics.MatcherProcessingResult.RegularExpr
import dev.mokkery.plugin.fir.acceptsMatcher
import dev.mokkery.plugin.fir.isCompositeMatcher
import dev.mokkery.plugin.fir.isMatcher
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
import org.jetbrains.kotlin.fir.resolve.providers.getRegularClassSymbolByClassId
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.types.constructType
import org.jetbrains.kotlin.fir.types.isSubtypeOf
import org.jetbrains.kotlin.fir.types.resolvedType

class MatchersProcessor(private val session: FirSession) {

    private val matchers = mutableMapOf<FirBasedSymbol<*>, MatcherProcessingResult>()

    fun processVariable(variable: FirVariable): MatcherProcessingResult = matchers.getOrPut(variable.symbol) {
        when {
            variable.symbol.acceptsMatcher(session) -> MatcherExpr(Regular)
            variable.initializer != null -> extractMatcherType(variable.initializer)
                ?.let { MatcherExpr(it) }
                ?: RegularExpr
            else -> RegularExpr
        }
    }

    fun extractAllMatcherTypes(expression: FirExpression?): Sequence<FirMatcherType> = sequence {
        expression ?: return@sequence
        when (val unwrappedExpression = expression.unwrapExpressionOrArgument()) {
            is FirFunctionCall -> extractMatcherType(unwrappedExpression)?.let { yield(it) }
            is FirQualifiedAccessExpression -> unwrappedExpression.calleeReference
                .toResolvedCallableSymbol()
                ?.let(matchers::get)
                ?.let { it as? MatcherExpr }
                ?.let { yield(it.type) }
            is FirWhenExpression -> unwrappedExpression.branches.forEach { branch ->
                yieldAll(
                    extractAllMatcherTypes(
                        branch
                            .result
                            .lastExpression
                    )
                )
            }
            is FirVarargArgumentsExpression -> unwrappedExpression
                .arguments
                .forEach { yieldAll(extractAllMatcherTypes(it)) }
            else -> Unit
        }
    }

    fun extractMatcherType(call: FirFunctionCall): FirMatcherType? {
        val callee = call.calleeReference as? FirResolvedNamedReference ?: return null
        val symbol = callee.resolvedSymbol as? FirNamedFunctionSymbol ?: return null
        return matchers.getOrPut(symbol) {
            when {
                !symbol.isMatcher() -> RegularExpr
                symbol.callableId == Mokkery.Callable.matchesComposite -> MatcherExpr(Composite)
                symbol.callableId == Mokkery.Callable.matches && symbol.valueParameterSymbols.size == 1 -> {
                    val compositeType = session
                        .getRegularClassSymbolByClassId(Mokkery.ClassId.ArgMatcherComposite)!!
                        .constructType(arrayOf(call.resolvedType))
                    val arg = call.arguments[0]
                    MatcherExpr(FirMatcherType.of(arg.resolvedType.isSubtypeOf(compositeType, session)))
                }
                else -> MatcherExpr(FirMatcherType.of(symbol.isCompositeMatcher(session)))
            }
        }.let { it as? MatcherExpr }?.type
    }

    fun extractMatcherType(expression: FirExpression?): FirMatcherType? {
        val types = extractAllMatcherTypes(expression).iterator()
        if (!types.hasNext()) return null
        do {
            val type = types.next()
            if (type == Composite) return Composite
        } while (types.hasNext())
        return Regular
    }

    fun getResultFor(symbol: FirBasedSymbol<*>): MatcherProcessingResult? = matchers[symbol]
}

fun MatchersProcessor.isMatcher(expression: FirExpression?): Boolean = extractAllMatcherTypes(expression).any()

fun MatchersProcessor.isMatcher(call: FirFunctionCall): Boolean = extractMatcherType(call) != null

interface MatcherProcessingResult {

    object RegularExpr : MatcherProcessingResult

    data class MatcherExpr(
        val type: FirMatcherType
    ) : MatcherProcessingResult
}

enum class FirMatcherType {
    Regular, Composite;

    companion object {
        fun of(isComposite: Boolean): FirMatcherType = when (isComposite) {
            true -> Composite
            false -> Regular
        }
    }
}
