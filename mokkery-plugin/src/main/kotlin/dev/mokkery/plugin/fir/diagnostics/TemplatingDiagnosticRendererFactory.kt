package dev.mokkery.plugin.fir.diagnostics

import dev.mokkery.plugin.fir.diagnostics.TemplatingChecker.Diagnostics
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
        it.put(
            factory = Diagnostics.FUNCTIONAL_PARAM_MUST_BE_REFERENCE,
            message = "Argument passed to ''{0}'' for param ''{1}'' must be a method reference expression.",
            rendererA = CommonRenderers.NAME,
            rendererB = FirDiagnosticRenderers.SYMBOL,
        )
        it.put(
            factory = Diagnostics.FUNCTION_REFERENCE_INCORRECT_TYPE,
            message = "Function reference passed to ''{0}'' is {1} but {2} is expected.",
            rendererA = CommonRenderers.NAME,
            rendererB = CommonRenderers.STRING,
            rendererC = CommonRenderers.STRING,
        )
        it.put(
            factory = Diagnostics.FUNCTION_REFERENCE_NOT_BOUND,
            message = "Function reference passed to ''{0}'' must be bound to a dispatch receiver.",
            rendererA = CommonRenderers.NAME,
        )
    }
}
