package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery.Callable
import org.jetbrains.kotlin.AbstractKtSourceElement
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.error2
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirFunctionCallChecker
import org.jetbrains.kotlin.fir.expressions.FirAnonymousFunctionExpression
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirFunctionCall
import org.jetbrains.kotlin.fir.expressions.arguments
import org.jetbrains.kotlin.fir.expressions.unwrapArgument
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtElement

class TemplatingChecker(
    private val session: FirSession,
    configuration: CompilerConfiguration,
) : FirFunctionCallChecker(MppCheckerKind.Common) {
    private val every = Callable.every
    private val everySuspend = Callable.everySuspend
    private val verify = Callable.verify
    private val verifySuspend = Callable.verifySuspend

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirFunctionCall) {
        val callee = expression.calleeReference as? FirResolvedNamedReference ?: return
        val symbol = callee.resolvedSymbol as? FirNamedFunctionSymbol ?: return
        context(symbol) {
            when (symbol.callableId) {
                every, everySuspend, verify, verifySuspend -> checkTemplating(expression)
            }
        }
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkTemplating(expression: FirFunctionCall) {
        val blockArgument = expression
            .arguments
            .last()
            .unwrapArgument()
        checkTemplatingFunctionalParam(blockArgument.source ?: expression.source, blockArgument)
    }

    context(context: CheckerContext, reporter: DiagnosticReporter, funSymbol: FirNamedFunctionSymbol)
    private fun checkTemplatingFunctionalParam(source: AbstractKtSourceElement?, blockArgument: FirExpression) {
        if (blockArgument !is FirAnonymousFunctionExpression) {
            return reporter.reportOn(
                source = source,
                factory = Diagnostics.FUNCTIONAL_PARAM_MUST_BE_LAMBDA,
                a = funSymbol.name,
                b = funSymbol.valueParameterSymbols.last(),
            )
        }
    }

    object Diagnostics {
        val FUNCTIONAL_PARAM_MUST_BE_LAMBDA by error2<KtElement, Name, FirValueParameterSymbol>()
    }
}
