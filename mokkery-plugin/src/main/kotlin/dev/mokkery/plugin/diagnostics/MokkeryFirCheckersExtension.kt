package dev.mokkery.plugin.diagnostics

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

class MokkeryFirCheckersExtension(
    session: FirSession,
    configuration: CompilerConfiguration,
) : FirAdditionalCheckersExtension(session) {

    override val expressionCheckers = object : ExpressionCheckers() {
        override val functionCallCheckers = setOf(MokkeryCallsChecker(session, configuration))
    }
}
