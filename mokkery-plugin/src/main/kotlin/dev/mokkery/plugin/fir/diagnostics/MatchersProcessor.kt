package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.Mokkery
import dev.mokkery.plugin.fir.allNonDispatchArgumentsMapping
import dev.mokkery.plugin.fir.contextFunctionIds
import dev.mokkery.plugin.fir.hasMatcherScope
import dev.mokkery.plugin.fir.isSpread
import dev.mokkery.plugin.fir.unwrapExpressionOrArgument
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.declarations.FirVariable
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.expressions.FirCheckedSafeCallSubject
import org.jetbrains.kotlin.fir.expressions.FirElvisExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirQualifiedAccessExpression
import org.jetbrains.kotlin.fir.expressions.FirReturnExpression
import org.jetbrains.kotlin.fir.expressions.FirSafeCallExpression
import org.jetbrains.kotlin.fir.expressions.FirThisReceiverExpression
import org.jetbrains.kotlin.fir.expressions.FirVarargArgumentsExpression
import org.jetbrains.kotlin.fir.expressions.FirWhenExpression
import org.jetbrains.kotlin.fir.lastExpression
import org.jetbrains.kotlin.fir.references.toResolvedBaseSymbol
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol
import org.jetbrains.kotlin.fir.references.toResolvedNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.visitors.FirVisitorVoid

class MatchersProcessor(matcherParameters: List<FirBasedSymbol<*>>, function: FirFunction) {

    private val matchers = matcherParameters.associateWithTo(mutableMapOf<FirBasedSymbol<*>, Boolean>()) { true }
    private val matchersVarargs = matcherParameters.filterTo(mutableSetOf()) { it is FirValueParameterSymbol && it.isVararg }
    private val localVariables = function.collectLocalVariables()

    fun processVariable(variable: FirVariable) = processValue(variable.symbol, variable.initializer)

    fun processValue(symbol: FirBasedSymbol<*>, value: FirExpression?) {
        matchers.getOrPut(symbol) { isMatcher(value) }
        if (isMatchersVararg(value)) matchersVarargs += symbol
    }

    fun isMatcherVariable(symbol: FirBasedSymbol<*>): Boolean? {
        if (symbol !in matchers && symbol in localVariables) {
            processValue(symbol, (symbol as FirPropertySymbol).resolvedInitializer)
        }
        return matchers[symbol]
    }

    fun processContextFunction(call: FirQualifiedAccessExpression): FirAnonymousFunction? {
        val symbol = call.calleeReference.toResolvedNamedFunctionSymbol() ?: return null
        if (symbol.callableId !in contextFunctionIds) return null
        val arguments = call.allNonDispatchArgumentsMapping(symbol)
        val block = arguments[symbol.valueParameterSymbols.last()]
            ?.unwrapExpressionOrArgument()
            ?.let { it as? FirAnonymousFunctionExpression }
            ?.anonymousFunction
            ?: return null
        val values = listOfNotNull(call.extensionReceiver) + symbol.valueParameterSymbols.dropLast(1).mapNotNull(arguments::get)
        val blockParameters = listOfNotNull(block.receiverParameter?.symbol) + block.contextParameters.map { it.symbol }
        blockParameters.zip(values, ::processValue)
        return block
    }

    fun isMatcher(expression: FirExpression?): Boolean = expression.anyValue {
        when (it) {
            is FirCallableReferenceAccess -> false
            is FirThisReceiverExpression -> it.calleeReference.boundSymbol?.let(matchers::get) == true
            is FirQualifiedAccessExpression -> {
                val symbol = it.calleeReference.toResolvedCallableSymbol()
                val block = processContextFunction(it)
                when {
                    symbol == null -> false
                    block != null -> block.returnedValues().any(::isMatcher)
                    else -> isMatcherVariable(symbol) == true || symbol.isMatcherBuilder()
                }
            }
            is FirVarargArgumentsExpression -> it.arguments.any(::isMatcher)
            else -> false
        }
    }

    fun isMatchersVararg(expression: FirExpression?): Boolean = expression?.isSpread() != true && expression.anyValue {
        it is FirQualifiedAccessExpression && it.calleeReference.toResolvedBaseSymbol()?.also(::isMatcherVariable) in matchersVarargs
    }

    private fun FirExpression?.anyValue(predicate: (FirExpression) -> Boolean): Boolean {
        return when (val unwrappedExpression = this?.unwrapExpressionOrArgument()) {
            null -> false
            is FirElvisExpression -> unwrappedExpression.rhs.anyValue(predicate)
            is FirWhenExpression -> unwrappedExpression.branches.any { it.result.lastExpression.anyValue(predicate) }
            is FirSafeCallExpression -> (unwrappedExpression.selector as? FirExpression).anyValue(predicate)
            is FirCheckedSafeCallSubject -> unwrappedExpression.originalReceiverRef.value.anyValue(predicate)
            else -> predicate(unwrappedExpression)
        }
    }
}

private fun FirFunction.collectLocalVariables(): Set<FirPropertySymbol> = buildSet {
    body?.accept(object : FirVisitorVoid() {
        override fun visitElement(element: FirElement) {
            if (element is FirFunction && element !is FirAnonymousFunction && element.symbol.hasMatcherScope()) return
            element.acceptChildren(this)
        }

        override fun visitProperty(property: FirProperty) {
            add(property.symbol)
            property.acceptChildren(this)
        }
    })
}

private fun FirAnonymousFunction.returnedValues(): List<FirExpression> = buildList {
    val function = this@returnedValues
    body?.accept(object : FirVisitorVoid() {
        override fun visitElement(element: FirElement) = element.acceptChildren(this)

        override fun visitReturnExpression(returnExpression: FirReturnExpression) {
            if (returnExpression.target.labeledElement == function) add(returnExpression.result)
            returnExpression.acceptChildren(this)
        }
    })
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
