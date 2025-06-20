package dev.mokkery.plugin.diagnostics

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.checkers.declaration.DeclarationCheckers
import org.jetbrains.kotlin.fir.analysis.checkers.expression.ExpressionCheckers
import org.jetbrains.kotlin.fir.analysis.extensions.FirAdditionalCheckersExtension

class MokkeryFirCheckersExtension(
    session: FirSession,
    configuration: CompilerConfiguration,
) : FirAdditionalCheckersExtension(session) {

    override val expressionCheckers = object : ExpressionCheckers() {
        override val functionCallCheckers = setOf(
            MocksCreationChecker(configuration),
            TemplatingChecker(configuration),
        )
    }

    override val declarationCheckers = object : DeclarationCheckers() {
        override val functionCheckers = setOf(
            MatchersDeclarationChecker(configuration),
            TemplatingDeclarationChecker(configuration)
        )
    }
}
