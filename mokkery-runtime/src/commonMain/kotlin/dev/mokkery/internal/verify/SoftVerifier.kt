package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults

internal class SoftVerifier(
    private val atLeast: Int,
    private val atMost: Int,
    private val callMatcher: CallMatcher,
    private val errorRenderer: Renderer<Error>,
) : Verifier {

    override fun verify(
        callTraces: List<CallTrace>,
        callTemplates: List<CallTemplate>
    ) = callTemplates.flatMap { template ->
        val matching = callTraces.filter { callMatcher.match(it, template).isMatching }
        if (matching.size !in atLeast..atMost) {
            val error = Error(
                expectedAtLeast = atLeast,
                expectedAtMost = atMost,
                templateMatchingResults = TemplateGroupedMatchingResults(
                    template = template,
                    calls = callTraces.groupBy { callMatcher.match(it, template) }
                )
            )
            throw AssertionError(errorRenderer.render(error))
        }
        matching
    }

    data class Error(
        val expectedAtLeast: Int,
        val expectedAtMost: Int,
        val templateMatchingResults: TemplateGroupedMatchingResults,
    )
}

