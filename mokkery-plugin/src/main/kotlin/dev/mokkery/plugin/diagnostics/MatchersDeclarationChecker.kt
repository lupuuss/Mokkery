package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.fir.isMatcher
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.MppCheckerKind
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.FirFunctionChecker
import org.jetbrains.kotlin.fir.declarations.FirFunction

class MatchersDeclarationChecker(
    private val session: FirSession,
    private val configuration: CompilerConfiguration,
) : FirFunctionChecker(MppCheckerKind.Common) {

    context(context: CheckerContext, reporter: DiagnosticReporter)
    override fun check(declaration: FirFunction) {
        if (!declaration.symbol.isMatcher()) return
        val visitor = MatchersUsageReporterVisitor(
            session = session,
            context = context,
            reporter = reporter,
            configuration = configuration,
            parentFunction = declaration.symbol,
            usageContext = MatchersUsageContext.BUILDER
        )
        declaration.accept(visitor)
    }
}
