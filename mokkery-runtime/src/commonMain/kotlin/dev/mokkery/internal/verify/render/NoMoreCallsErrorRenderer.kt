package dev.mokkery.internal.verify.render

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.render.Renderers
import dev.mokkery.internal.tracing.CallTrace

internal class NoMoreCallsErrorRenderer(
    private val instanceIdRenderer: Renderer<MokkeryInstanceId>,
    private val callsListRenderer: Renderer<List<CallTrace>>,
) : Renderer<Pair<MokkeryInstanceId, List<CallTrace>>> {

    override fun render(value: Pair<MokkeryInstanceId, List<CallTrace>>) = buildString {
        val (id, calls) = value
        appendLine("Unverified calls for ${instanceIdRenderer.render(id)}:")
        append(callsListRenderer.render(calls))
    }

    companion object {

        fun lazy(
            nameShortener: NameShortener,
            collection: MokkeryCollection,
            renderers: Renderers,
        ) = lazyVerifyRenderer(nameShortener, collection, renderers) {
            NoMoreCallsErrorRenderer(
                instanceIdRenderer = instanceIdAliasRenderer,
                callsListRenderer = pointsRenderer(item = callTraceAliasRenderer)
            )
        }
    }
}
