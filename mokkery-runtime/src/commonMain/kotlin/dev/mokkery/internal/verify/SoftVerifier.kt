package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults

internal class SoftVerifier(
    private val atLeast: Int,
    private val atMost: Int,
    private val callMatcher: CallMatcher,
) : Verifier {

    override fun verify(
        callTraces: List<CallTrace>,
        callTemplates: List<CallTemplate>
    ): Verifier.Result {
        val traces = callTemplates.flatMap { template ->
            val matching = callTraces.filter { callMatcher.match(it, template).isMatching }
            if (matching.size !in atLeast..atMost) {
                return failure(
                    expectedAtLeast = atLeast,
                    expectedAtMost = atMost,
                    templateMatchingResults = TemplateGroupedMatchingResults(
                        template = template,
                        calls = callTraces.groupBy { callMatcher.match(it, template) }
                    )
                )
            }
            matching
        }
        return Verifier.Result.Success(traces)
    }

    private fun failure(
        expectedAtLeast: Int,
        expectedAtMost: Int,
        templateMatchingResults: TemplateGroupedMatchingResults
    ) = Verifier.Result.Failure(
        Error(
            expectedAtLeast = expectedAtLeast,
            expectedAtMost = expectedAtMost,
            templateMatchingResults = templateMatchingResults
        )
    )

    data class Error(
        val expectedAtLeast: Int,
        val expectedAtMost: Int,
        val templateMatchingResults: TemplateGroupedMatchingResults,
    ) : Verifier.Error
}

