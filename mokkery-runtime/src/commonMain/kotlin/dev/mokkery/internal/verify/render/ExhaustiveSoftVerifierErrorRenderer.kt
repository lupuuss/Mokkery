package dev.mokkery.internal.verify.render

import dev.mokkery.rendering.Renderer
import dev.mokkery.internal.rendering.callTemplateRenderer
import dev.mokkery.internal.verify.ExhaustiveSoftVerifier
import dev.mokkery.rendering.MokkeryRenderingScope

internal object ExhaustiveSoftVerifierErrorRenderer : Renderer<ExhaustiveSoftVerifier.Error> {

    override val key get() = VerifyRendering.exhaustiveSoftVerifierError

    context(scope: MokkeryRenderingScope)
    override fun render(value: ExhaustiveSoftVerifier.Error): String = buildString {
        when (value) {
            is ExhaustiveSoftVerifier.Error.NoMatch -> {
                val template = value.templateMatchingResults.template
                appendLine("No matching call for ${scope.callTemplateRenderer.render(template)}!")
                append(scope.templateGroupedMatchingResults.render(value.templateMatchingResults))
            }
            is ExhaustiveSoftVerifier.Error.UnverifiedCalls -> {
                append(scope.extraUnverifiedCalls.render(value.calls))
            }
        }
    }
}
