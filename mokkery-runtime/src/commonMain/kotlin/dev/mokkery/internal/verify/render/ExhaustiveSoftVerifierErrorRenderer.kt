package dev.mokkery.internal.verify.render

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.ExhaustiveSoftVerifier
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults

internal class ExhaustiveSoftVerifierErrorRenderer(
    private val templateRenderer: Renderer<CallTemplate>,
    private val matchingResultsRenderer: Renderer<TemplateGroupedMatchingResults>,
    private val unverifiedCallsRenderer: Renderer<List<CallTrace>>
) : Renderer<ExhaustiveSoftVerifier.Error> {

    override fun render(value: ExhaustiveSoftVerifier.Error): String = buildString {
        when (value) {
            is ExhaustiveSoftVerifier.Error.NoMatch -> {
                appendLine("No matching call for ${templateRenderer.render(value.templateMatchingResults.template)}!")
                append(matchingResultsRenderer.render(value.templateMatchingResults))
            }
            is ExhaustiveSoftVerifier.Error.UnverifiedCalls -> {
                append(unverifiedCallsRenderer.render(value.calls))
            }
        }
    }

    companion object {

        fun factory(
            nameShortener: NameShortener,
            collection: MokkeryCollection
        ) = verifyRendererFactory(nameShortener, collection) {
            ExhaustiveSoftVerifierErrorRenderer(
                templateRenderer = callTemplateAliasRenderer,
                matchingResultsRenderer = templateGroupedMatchingResultsRenderer,
                unverifiedCallsRenderer = ExtraUnverifiedCallsRenderer(
                    traceListRenderer = pointsRenderer(callTraceAliasRenderer)
                )
            )
        }
    }
}
