package dev.mokkery.internal.verify.render

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.render.Renderers
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.NotVerifier

internal class NotVerifierErrorRenderer(
    private val templateRenderer: Renderer<CallTemplate>,
    private val traceListRenderer: Renderer<List<CallTrace>>,
) : Renderer<NotVerifier.Error> {

    override fun render(value: NotVerifier.Error) = buildString {
        appendLine("Calls to ${templateRenderer.render(value.template)} were not expected, but occurred:")
        append(traceListRenderer.render(value.unexpectedCalls))
    }

    companion object {

        fun factory(
            nameShortener: NameShortener,
            collection: MokkeryCollection
        ) = verifyRendererFactory(nameShortener, collection) {
            NotVerifierErrorRenderer(
                templateRenderer = callTemplateAliasRenderer,
                traceListRenderer = Renderers.points(item = callTraceAliasRenderer)
            )
        }
    }
}
