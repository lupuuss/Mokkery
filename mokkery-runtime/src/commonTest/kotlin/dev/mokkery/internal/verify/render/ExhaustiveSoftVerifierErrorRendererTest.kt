package dev.mokkery.internal.verify.render

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.ExhaustiveSoftVerifier
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test

class ExhaustiveSoftVerifierErrorRendererTest {

    private val templateRenderer = TestRenderer<CallTemplate> { "CALL_TEMPLATE" }
    private val matchingResultsRenderer = TestRenderer<TemplateGroupedMatchingResults> { "MATCHING_RESULTS" }
    private val unverifiedCallsRenderer = TestRenderer<List<CallTrace>> { "UNVERIFIED_CALLS" }
    private val errorRenderer = ExhaustiveSoftVerifierErrorRenderer(
        templateRenderer = templateRenderer,
        matchingResultsRenderer = matchingResultsRenderer,
        unverifiedCallsRenderer = unverifiedCallsRenderer
    )

    @Test
    fun testRendersNotMatchingCallWithResults() {
        val results = TemplateGroupedMatchingResults(
            template = fakeCallTemplate(),
            calls = mapOf(CallMatchResult.NotMatching to listOf(fakeCallTrace()))
        )
        errorRenderer.assert(ExhaustiveSoftVerifier.Error.NoMatch(results)) {
            """
                No matching call for CALL_TEMPLATE!
                MATCHING_RESULTS
            """.trimIndent()
        }
    }

    @Test
    fun testRendersUnverifiedCallsWhenUnverifiedCallsError() {
        errorRenderer.assert(ExhaustiveSoftVerifier.Error.UnverifiedCalls(listOf(fakeCallTrace()))) {
            """
                UNVERIFIED_CALLS
            """.trimIndent()
        }
    }
}
