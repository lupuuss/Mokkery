package dev.mokkery.plugin.diagnostics

import dev.mokkery.plugin.core.Mokkery
import dev.mokkery.plugin.core.Mokkery.TemplatingErrors
import dev.mokkery.plugin.diagnostics.TemplatingChecker.Diagnostics
import org.jetbrains.kotlin.diagnostics.KtDiagnosticFactoryToRendererMap
import org.jetbrains.kotlin.diagnostics.rendering.BaseDiagnosticRendererFactory
import org.jetbrains.kotlin.diagnostics.rendering.CommonRenderers
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticParameterRenderer
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirDiagnosticRenderers
import org.jetbrains.kotlin.fir.types.ConeKotlinType

class TempltingDiagnosticRendererFactory : BaseDiagnosticRendererFactory() {

    private val typeRenderer: DiagnosticParameterRenderer<ConeKotlinType> = FirDiagnosticRenderers.RENDER_TYPE

    override val MAP = KtDiagnosticFactoryToRendererMap("MokkeryTemplatingDiagnostic").apply {
        put(
            factory = Diagnostics.FUNCTIONAL_PARAM_MUST_BE_LAMBDA,
            message = TemplatingErrors.notLambdaExpression(functionName = "{0}", param = "{1}"),
            rendererA = CommonRenderers.NAME,
            rendererB = FirDiagnosticRenderers.SYMBOL,
        )
    }
}
