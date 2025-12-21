package dev.mokkery.internal.verify.render

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.render.Renderers
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.verify.OrderVerifier
import dev.mokkery.internal.verify.results.TemplateMatchingResult

internal class OrderVerifierErrorRenderer(
    private val templateRenderer: Renderer<CallTemplate>,
    private val matchingResultsRenderer: Renderer<List<TemplateMatchingResult>>,
) : Renderer<OrderVerifier.Error> {

    override fun render(value: OrderVerifier.Error) = buildString {
        append("Expected calls in specified order but not satisfied! ")
        appendLine("Failed at ${value.failedIndex + 1}. ${templateRenderer.render(value.failedAt)}!")
        append(matchingResultsRenderer.render(value.results))
    }

    companion object {

        fun lazy(
            nameShortener: NameShortener,
            collection: MokkeryCollection,
            renderers: Renderers
        ) = lazyVerifyRenderer(nameShortener, collection, renderers) {
            OrderVerifierErrorRenderer(
                templateRenderer = callTemplateAliasRenderer,
                matchingResultsRenderer = templateMatchingResultsRenderer
            )
        }

    }
}
