package dev.mokkery.internal.verify.render

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.verify.SoftVerifier
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults

internal class SoftVerifierErrorRenderer(
    private val templateRenderer: Renderer<CallTemplate>,
    private val matchingResultsRenderer: Renderer<TemplateGroupedMatchingResults>,
) : Renderer<SoftVerifier.Error> {

    override fun render(value: SoftVerifier.Error) = buildString {
        val atLeast = value.expectedAtLeast
        val atMost = value.expectedAtMost
        val callsCount = value.templateMatchingResults.calls[CallMatchResult.Matching]?.size ?: 0
        append("Expected ")
        val callsExpectations = when {
            atLeast == atMost -> "exactly $atLeast calls"
            atLeast != 1 && atMost != Int.MAX_VALUE -> "calls count to be in range $atLeast..$atMost"
            atLeast != 1 -> "at least $atLeast calls"
            atMost != Int.MAX_VALUE -> "at most $atMost calls"
            else -> "any call"
        }
        append(callsExpectations)
        if (callsCount == 0) {
            append(", but no matching calls")
        } else {
            append(", but $callsCount occurred")
        }
        appendLine(" for ${templateRenderer.render(value.templateMatchingResults.template)}!")
        append(matchingResultsRenderer.render(value.templateMatchingResults))
    }

    companion object {

        fun lazy(
            nameShortener: NameShortener,
            collection: MokkeryCollection
        ) = lazyVerifyRenderer(nameShortener, collection) {
            SoftVerifierErrorRenderer(
                templateRenderer = callTemplateAliasRenderer,
                matchingResultsRenderer = templateGroupedMatchingResultsRenderer
            )
        }
    }
}
