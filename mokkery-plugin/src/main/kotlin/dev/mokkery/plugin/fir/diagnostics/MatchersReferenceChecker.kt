package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.fir.diagnostics.MatchersUsageReporterVisitor.Diagnostics
import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.expression.FirExpressionChecker
import org.jetbrains.kotlin.fir.expressions.FirCallableReferenceAccess
import org.jetbrains.kotlin.fir.references.toResolvedCallableSymbol

/**
 * Rejects references to matcher declarations anywhere, because the compiled declarations
 * have signatures that differ from the referenced ones.
 */
class MatchersReferenceChecker : FirExpressionChecker<FirCallableReferenceAccess>(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(expression: FirCallableReferenceAccess) {
        if (expression.source?.kind is KtFakeSourceElementKind) return
        val symbol = expression.calleeReference.toResolvedCallableSymbol() ?: return
        if (!symbol.isMatcherLikeDeclaration()) return
        reporter.reportOn(expression.source, Diagnostics.ILLEGAL_MATCHER_REFERENCE)
    }
}
