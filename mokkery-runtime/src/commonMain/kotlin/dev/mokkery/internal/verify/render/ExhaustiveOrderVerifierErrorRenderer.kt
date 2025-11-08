package dev.mokkery.internal.verify.render

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.verify.ExhaustiveOrderVerifier
import dev.mokkery.internal.verify.results.TemplateMatchingResult

internal class ExhaustiveOrderVerifierErrorRenderer(
    private val matchingResultsRenderer: Renderer<List<TemplateMatchingResult>>
) : Renderer<ExhaustiveOrderVerifier.Error> {

    override fun render(value: ExhaustiveOrderVerifier.Error) = buildString {
        appendLine("Expected strict order of calls without unverified ones, but not satisfied!")
        append(matchingResultsRenderer.render(value.results))
    }

    companion object {

        fun factory(
            nameShortener: NameShortener,
            collection: MokkeryCollection
        ) = verifyRendererFactory(nameShortener, collection) {
            ExhaustiveOrderVerifierErrorRenderer(matchingResultsRenderer = templateMatchingResultsRenderer)
        }
    }
}
