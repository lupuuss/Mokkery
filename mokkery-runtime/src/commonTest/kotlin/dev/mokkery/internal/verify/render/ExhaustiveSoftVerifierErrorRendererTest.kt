package dev.mokkery.internal.verify.render

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.ExhaustiveSoftVerifier
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.testRendering
import kotlin.test.Test

class ExhaustiveSoftVerifierErrorRendererTest {

    private val templateRenderer = TestRenderer<CallTemplate>(MokkeryRendering.callTemplateKey) { "CALL_TEMPLATE" }
    private val matchingResultsRenderer = TestRenderer<TemplateGroupedMatchingResults>(
        VerifyRendering.templateGroupedMatchingResults
    ) { "MATCHING_RESULTS" }
    private val unverifiedCallsRenderer = TestRenderer<List<CallTrace>>(
        VerifyRendering.extraUnverifiedCalls
    ) { "UNVERIFIED_CALLS" }

    private val context = templateRenderer + matchingResultsRenderer + unverifiedCallsRenderer
    private val renderer = ExhaustiveSoftVerifierErrorRenderer

    @Test
    fun testRendersNotMatchingCallWithResults() {
        val results = TemplateGroupedMatchingResults(
            template = fakeCallTemplate(),
            calls = mapOf(CallMatchResult.NotMatching to listOf(fakeCallTrace()))
        )
        testRendering(context) {
            renderer.assert(ExhaustiveSoftVerifier.Error.NoMatch(results)) {
                """
                    No matching call for CALL_TEMPLATE!
                    MATCHING_RESULTS
                """.trimIndent()
            }
        }
    }

    @Test
    fun testRendersUnverifiedCallsWhenUnverifiedCallsError() {
        testRendering(context) {
            renderer.assert(ExhaustiveSoftVerifier.Error.UnverifiedCalls(listOf(fakeCallTrace()))) {
                "UNVERIFIED_CALLS"
            }
        }
    }
}
