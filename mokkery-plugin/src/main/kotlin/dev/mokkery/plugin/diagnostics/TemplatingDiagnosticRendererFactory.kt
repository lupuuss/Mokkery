package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.diagnostics.TemplatingChecker.Diagnostics
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers

class TemplatingDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    override val MAP by KtDiagnosticFactoryToRendererMap("MokkeryTemplatingDiagnostic") {
        it.put(
            factory = Diagnostics.FUNCTIONAL_PARAM_MUST_BE_LAMBDA,
            message = "Argument passed to ''{0}'' for param ''{1}'' must be a lambda expression.",
            rendererA = CommonRenderers.NAME,
            rendererB = FirDiagnosticRenderers.SYMBOL,
        )
    }
}
