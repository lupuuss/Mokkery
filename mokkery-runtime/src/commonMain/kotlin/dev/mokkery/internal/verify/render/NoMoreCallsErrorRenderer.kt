package dev.mokkery.internal.verify.render

import dev.mokkery.rendering.Renderer
import dev.mokkery.internal.rendering.callEntryRenderer
import dev.mokkery.internal.rendering.renderingFactory
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.rendering.MokkeryRenderingScope

internal object NoMoreCallsErrorRenderer : Renderer<List<CallTrace>> {

    context(scope: MokkeryRenderingScope)
    override fun render(value: List<CallTrace>): String = buildString {
        val callsListRenderer = scope.renderingFactory.points(item = scope.callEntryRenderer)
        appendLine("No unverified calls expected, but these are present:")
        append(callsListRenderer.render(value))
    }

    override val key get() = VerifyRendering.noMoreCalls
}
