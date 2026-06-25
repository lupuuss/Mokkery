package dev.mokkery.internal.verify.render

import dev.mokkery.internal.rendering.Renderer
import dev.mokkery.internal.rendering.callTemplateRenderer
import dev.mokkery.internal.verify.ExhaustiveSoftVerifier
import dev.mokkery.rendering.MokkeryRenderingScope

internal object ExhaustiveSoftVerifierErrorRenderer : Renderer<ExhaustiveSoftVerifier.Error> {

    override val key get() = VerifyRendering.exhaustiveSoftVerifierError

    context(scope: MokkeryRenderingScope)
    override fun render(value: ExhaustiveSoftVerifier.Error): String = buildString {
        when (value) {
            is ExhaustiveSoftVerifier.Error.NoMatch -> {
                appendLine("No matching call for ${callTemplateRenderer.render(value.templateMatchingResults.template)}!")
                append(templateGroupedMatchingResults.render(value.templateMatchingResults))
            }
            is ExhaustiveSoftVerifier.Error.UnverifiedCalls -> {
                append(extraUnverifiedCalls.render(value.calls))
            }
        }
    }
}
