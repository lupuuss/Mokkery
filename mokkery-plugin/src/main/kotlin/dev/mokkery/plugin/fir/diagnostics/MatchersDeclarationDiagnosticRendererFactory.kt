package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.fir.diagnostics.MatchersDeclarationChecker.Diagnostics
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers

class MatchersDeclarationDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    override val MAP by KtDiagnosticFactoryToRendererMap("MokkeryMatchersDeclarationDiagnostic") {
        it.put(
            factory = Diagnostics.MATCHER_MUST_BE_FINAL,
            message = "Matcher must be final."
        )
        it.put(
            factory = Diagnostics.MATCHER_MUST_NOT_OVERRIDE,
            message = "Matcher must not override other members."
        )
        it.put(
            factory = Diagnostics.MATCHER_MUST_NOT_BE_EXTERNAL,
            message = "Matcher must not be external."
        )
        it.put(
            factory = Diagnostics.PARAM_OF_TYPE_CANNOT_BE_MARKED_MATCHER,
            message = "Parameter of type ''{0}'' cannot be marked with @dev.mokkery.annotations.Matcher",
            rendererA = FirDiagnosticRenderers.RENDER_TYPE
        )
        it.put(
            factory = Diagnostics.MATCHER_MUST_BE_REGULAR_FUNCTION,
            message = "Matcher must be a regular function or a getter of a non-delegated 'val'."
        )
        it.put(
            factory = Diagnostics.MATCHER_SCOPE_MUST_NOT_BE_NULLABLE,
            message = "MokkeryMatcherScope must not be nullable."
        )
        it.put(
            factory = Diagnostics.MATCHER_SCOPE_MUST_NOT_BE_TYPE_PARAMETER,
            message = "MokkeryMatcherScope must not be provided through a type parameter."
        )
    }
}
