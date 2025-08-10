package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.diagnostics.MatchersDeclarationChecker.Diagnostics
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers

class MatchersDeclarationDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    override val MAP = KtDiagnosticFactoryToRendererMap("MokkeryMatchersDeclarationDiagnostic").apply {
        put(
            factory = Diagnostics.MATCHER_MUST_BE_FINAL,
            message = Mokkery.MatchersDeclarationErrors.matcherMustBeFinal()
        )
        put(
            factory = Diagnostics.MATCHER_MUST_NOT_BE_EXTERNAL,
            message = Mokkery.MatchersDeclarationErrors.matcherMustNotBeExternal()
        )
        put(
            factory = Diagnostics.MATCHER_MUST_BE_REGULAR_FUNCTION,
            message = Mokkery.MatchersDeclarationErrors.matcherMustBeRegularFunction()
        )
        put(
            factory = Diagnostics.PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER,
            message = Mokkery.MatchersDeclarationErrors.argMatcherTypeCannotBeMarkedMatcher("{0}"),
            rendererA = FirDiagnosticRenderers.RENDER_TYPE
        )
    }
}
