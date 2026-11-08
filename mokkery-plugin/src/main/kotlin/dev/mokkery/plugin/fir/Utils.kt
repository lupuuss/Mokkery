package dev.mokkery.plugin.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.SessionHolder
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.InlineStatus
import org.jetbrains.kotlin.fir.expressions.FirCall
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirSpreadArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirWrappedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirWrappedExpression
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.resolvedArgumentMapping
import org.jetbrains.kotlin.fir.expressions.unwrapExpression
import org.jetbrains.kotlin.fir.resolve.isArrayOfCall
import org.jetbrains.kotlin.fir.resolve.toClassLikeSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol

context(session: SessionHolder)
fun FirCallableSymbol<*>.dispatchReceiverClassLikeSymbolOrNull() = dispatchReceiverType?.toClassLikeSymbol()

fun FirFunction.isInlinedInPlace() = this is FirAnonymousFunction && inlineStatus == InlineStatus.Inline

fun FirQualifiedAccessExpression.allNonDispatchArgumentsMapping(
    symbol: FirCallableSymbol<*>
): Map<FirBasedSymbol<*>, FirExpression> {
    val contextSymbols = symbol.contextParameterSymbols
    val receiverParameterSymbol = symbol.receiverParameterSymbol
    val map = LinkedHashMap<FirBasedSymbol<*>, FirExpression>(
        contextSymbols.size
                + ((symbol as? FirFunctionSymbol<*>)?.valueParameterSymbols?.size ?: 0)
                + if (receiverParameterSymbol != null) 1 else 0
    )
    for ((i, element) in contextSymbols.withIndex()) {
        contextArguments.getOrNull(i)?.let { map[element] = it }
    }
    val extensionReceiver = extensionReceiver
    if (receiverParameterSymbol != null && extensionReceiver != null) {
        map[receiverParameterSymbol] = extensionReceiver
    }
    (this as? FirFunctionCall)?.resolvedArgumentMapping?.forEach { (arg, param) ->
        map[param.symbol] = arg
    }
    return map
}

fun FirQualifiedAccessExpression.nonDispatchArguments(): List<FirExpression> = buildList {
    addAll(contextArguments)
    extensionReceiver?.let(::add)
    if (this@nonDispatchArguments is FirCall) addAll(arguments)
}

fun FirExpression.unwrapExpressionOrArgument(): FirExpression {
    return when (this) {
        is FirWrappedExpression -> expression.unwrapExpressionOrArgument()
        else -> unwrapExpression()
    }
}

fun FirExpression.isSpread(): Boolean {
    return when (this) {
        is FirSpreadArgumentExpression -> true
        is FirWrappedArgumentExpression -> expression.isSpread()
        else -> {
            val unwrapped = unwrapExpression()
            if (unwrapped !== this) return unwrapped.isSpread()
            false
        }
    }
}

fun FirExpression.extractArrayLiteralCall(
    session: FirSession
): FirFunctionCall? = when (val expression = unwrapExpressionOrArgument()) {
    is FirFunctionCall if expression.isArrayOfCall(session) -> expression
    else -> null
}
