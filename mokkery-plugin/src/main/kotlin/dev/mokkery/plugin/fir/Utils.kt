package dev.mokkery.plugin.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.declaredFunctions
import org.jetbrains.kotlin.fir.declarations.declaredProperties
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.FirSpreadArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirWrappedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.resolvedArgumentMapping
import org.jetbrains.kotlin.fir.expressions.unwrapExpression
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.FirArrayOfCallTransformer.Companion.isArrayOfCall
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirRegularClassSymbol

fun FirRegularClassSymbol.declaredMembers(session: FirSession): List<FirBasedSymbol<*>> {
    return declaredFunctions(session) + declaredProperties(session)
}

fun FirFunctionCall.allNonDispatchArgumentsMapping(symbol: FirFunctionSymbol<*>): Map<FirBasedSymbol<*>, FirExpression> {
    val contextSymbols = symbol.contextParameterSymbols
    val receiverParameterSymbol = symbol.receiverParameterSymbol
    val map = LinkedHashMap<FirBasedSymbol<*>, FirExpression>(
        contextSymbols.size
                + symbol.valueParameterSymbols.size
                + if (receiverParameterSymbol != null) 1 else 0
    )
    for (i in 0..<contextSymbols.size) {
        map[contextSymbols[i]] = contextArguments[i]
    }
    if (receiverParameterSymbol != null) {
        map[receiverParameterSymbol] = extensionReceiver!!
    }
    resolvedArgumentMapping?.forEach { (arg, param) ->
        map[param.symbol] = arg
    }
    return map
}

fun FirExpression.unwrapExpressionOrArgument(): FirExpression {
    return when (this) {
        is FirWrappedArgumentExpression -> expression.unwrapExpressionOrArgument()
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
