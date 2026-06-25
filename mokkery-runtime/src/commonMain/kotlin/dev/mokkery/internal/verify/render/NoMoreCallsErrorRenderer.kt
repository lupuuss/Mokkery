package dev.mokkery.internal.verify.render

import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.rendering.Renderer
import dev.mokkery.internal.rendering.callTraceRenderer
import dev.mokkery.internal.rendering.factory
import dev.mokkery.internal.rendering.instanceIdRenderer
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.rendering.MokkeryRenderingScope

internal object NoMoreCallsErrorRenderer : Renderer<Pair<MokkeryInstanceId, List<CallTrace>>> {

    context(scope: MokkeryRenderingScope)
    override fun render(value: Pair<MokkeryInstanceId, List<CallTrace>>) = buildString {
        val callsListRenderer = factory.points(item = callTraceRenderer)
        val (id, calls) = value
        appendLine("Unverified calls for ${instanceIdRenderer.render(id)}:")
        append(callsListRenderer.render(calls))
    }

    override val key get() = VerifyRendering.noMoreCalls
}
