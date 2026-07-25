package dev.mokkery.internal.verify.render

import dev.mokkery.internal.rendering.Renderer
import dev.mokkery.internal.rendering.callTemplateRenderer
import dev.mokkery.internal.verify.OrderVerifier
import dev.mokkery.rendering.MokkeryRenderingScope

internal object OrderVerifierErrorRenderer : Renderer<OrderVerifier.Error> {

    override val key get() = VerifyRendering.orderVerifierError

    context(scope: MokkeryRenderingScope)
    override fun render(value: OrderVerifier.Error) = buildString {
        append("Expected calls in specified order but not satisfied! ")
        appendLine("Failed at ${value.failedIndex + 1}. ${scope.callTemplateRenderer.render(value.failedAt)}!")
        append(scope.templateMatchingResults.render(value.results))
    }
}
