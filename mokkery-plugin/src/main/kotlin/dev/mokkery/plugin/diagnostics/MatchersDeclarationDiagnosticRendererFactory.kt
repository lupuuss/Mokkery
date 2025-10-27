package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.diagnostics.MatchersDeclarationChecker.Diagnostics
import dev.mokkery.plugin.fir.KtDiagnosticFactoryToRendererMapCompat
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers

class MatchersDeclarationDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    override val MAP by KtDiagnosticFactoryToRendererMapCompat("MokkeryMatchersDeclarationDiagnostic") {
        put(
            factory = Diagnostics.MATCHER_MUST_BE_FINAL,
            message = "Matcher must be final."
        )
        put(
            factory = Diagnostics.MATCHER_MUST_NOT_BE_EXTERNAL,
            message = "Matcher must not be external."
        )
        put(
            factory = Diagnostics.PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER,
            message = "Parameter of type ''{0}'' cannot be marked with @dev.mokkery.annotations.Matcher",
            rendererA = FirDiagnosticRenderers.RENDER_TYPE
        )
        put(
            factory = Diagnostics.MATCHER_MUST_BE_REGULAR_FUNCTION,
            message = "Matcher must be a regular function."
        )
    }
}
