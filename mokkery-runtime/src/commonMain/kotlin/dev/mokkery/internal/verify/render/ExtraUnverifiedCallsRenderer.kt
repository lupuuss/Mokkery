package dev.mokkery.internal.verify.render

import dev.mokkery.internal.rendering.Renderer
import dev.mokkery.internal.rendering.callTraceRenderer
import dev.mokkery.internal.rendering.factory
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.rendering.MokkeryRenderingScope

internal object ExtraUnverifiedCallsRenderer : Renderer<List<CallTrace>> {

    override val key get() = VerifyRendering.extraUnverifiedCalls

    context(scope: MokkeryRenderingScope)
    override fun render(value: List<CallTrace>): String = buildString {
        val traceListRenderer = factory.points(item = callTraceRenderer)
        appendLine("All expected calls have been satisfied! However, there should not be any unverified calls, yet these are present:")
        append(traceListRenderer.render(value))
    }
}
