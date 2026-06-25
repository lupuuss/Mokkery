package dev.mokkery.internal.verify.render

import dev.mokkery.internal.rendering.Renderer
import dev.mokkery.internal.rendering.callTemplateRenderer
import dev.mokkery.internal.rendering.callTraceRenderer
import dev.mokkery.internal.rendering.factory
import dev.mokkery.internal.verify.NotVerifier
import dev.mokkery.rendering.MokkeryRenderingScope

internal object NotVerifierErrorRenderer : Renderer<NotVerifier.Error> {

    override val key get() = VerifyRendering.notVerifierError

    context(scope: MokkeryRenderingScope)
    override fun render(value: NotVerifier.Error) = buildString {
        val traceListRenderer = factory.points(item = callTraceRenderer)
        appendLine("Calls to ${callTemplateRenderer.render(value.template)} were not expected, but occurred:")
        append(traceListRenderer.render(value.unexpectedCalls))
    }

}
