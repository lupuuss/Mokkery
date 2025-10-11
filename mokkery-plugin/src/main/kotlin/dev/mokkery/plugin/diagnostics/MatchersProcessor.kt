package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery
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
import org.jetbrains.kotlin.fir.resolve.defaultType
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
            variable.symbol.acceptsMatcher(session) -> MatcherProcessingResult.MatcherExpression(
                matcherType(isComposite = false, isVararg = false)
            )
            variable.initializer != null -> extractMatcherType(variable.initializer)
                ?.let { MatcherProcessingResult.MatcherExpression(it) }
                ?: RegularExpression
            else -> RegularExpression
        }
    }

    fun extractAllMatcherTypes(expression: FirExpression?): Sequence<FirMatcherType> = sequence {
        expression ?: return@sequence
        when (val unwrappedExpression = expression.unwrapExpressionOrArgument()) {
            is FirFunctionCall -> extractMatcherType(unwrappedExpression)?.let { yield(it) }
            is FirQualifiedAccessExpression -> unwrappedExpression.calleeReference
                .toResolvedCallableSymbol()
                ?.let(matchers::get)
                ?.let { it as? MatcherProcessingResult.MatcherExpression }
                ?.let { yield(it.matcherType) }
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
                !symbol.isMatcher() -> RegularExpression
                symbol.callableId == Mokkery.Callable.matchesComposite -> {
                    val isVararg = call.arguments.any { arg -> extractAllMatcherTypes(arg).any { it.isVararg } }
                    val type = matcherType(true, isVararg)
                    MatcherProcessingResult.MatcherExpression(type)
                }
                symbol.callableId == Mokkery.Callable.matches && symbol.valueParameterSymbols.size == 1 -> {
                    val varargMatcherType = session
                        .getRegularClassSymbolByClassId(Mokkery.ClassId.VarArgMatcher)!!
                        .defaultType()
                    val compositeType = session
                        .getRegularClassSymbolByClassId(Mokkery.ClassId.ArgMatcherComposite)!!
                        .constructType(arrayOf(call.resolvedType))
                    val arg = call.arguments[0]
                    val isVararg = arg.resolvedType.isSubtypeOf(varargMatcherType, session)
                    val isComposite = arg.resolvedType.isSubtypeOf(compositeType, session)
                    MatcherProcessingResult.MatcherExpression(matcherType(isComposite, isVararg))
                }
                else -> {
                    val isComposite = symbol.isCompositeMatcher(session)
                    val isVararg = when {
                        isComposite -> call.arguments.any { arg -> extractAllMatcherTypes(arg).any { it.isVararg } }
                        else -> symbol.isVarargMatcher(session)
                    }
                    val type = matcherType(isComposite, isVararg)
                    MatcherProcessingResult.MatcherExpression(type)
                }
            }
        }.let { it as? MatcherProcessingResult.MatcherExpression }?.matcherType
    }

    fun extractMatcherType(expression: FirExpression?): FirMatcherType? {
        val processed = extractAllMatcherTypes(expression).toList()
        return when {
            processed.isEmpty() -> null
            else -> matcherType(
                isComposite = processed.all { it.isComposite },
                isVararg = processed.all { it.isVararg }
            )
        }
    }

    fun getResultFor(symbol: FirBasedSymbol<*>): MatcherProcessingResult? = matchers[symbol]

    private fun matcherType(isComposite: Boolean, isVararg: Boolean): FirMatcherType {
        return object : FirMatcherType {
            override val isComposite = isComposite
            override val isVararg = isVararg
            override val isRegular = !isComposite && !isVararg
        }
    }
}

fun MatchersProcessor.isMatcher(expression: FirExpression?): Boolean = extractAllMatcherTypes(expression).any()

fun MatchersProcessor.isMatcher(call: FirFunctionCall): Boolean = extractMatcherType(call) != null

fun MatchersProcessor.isVarargMatcher(expression: FirExpression?): Boolean = extractAllMatcherTypes(expression).any {
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
