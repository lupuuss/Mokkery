package dev.mokkery.internal.verify.render

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.verify.SoftVerifier
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

class SoftVerifierErrorRendererTest {

    private val templateRenderer = TestRenderer<CallTemplate> { "CALL_TEMPLATE" }
    private val matchingResultsRenderer = TestRenderer<TemplateGroupedMatchingResults> { "MATCHING_RESULTS" }
    private val errorRenderer = SoftVerifierErrorRenderer(
        templateRenderer = templateRenderer,
        matchingResultsRenderer = matchingResultsRenderer,
    )

    @Test
    fun testRendersCorrectMessageWhenSpecificRangeExpected() {
        test(atLeast = 3, atMost = 5, actualTracesCount = 1) {
            """
                Expected calls count to be in range 3..5, but 1 occurred for CALL_TEMPLATE!
                MATCHING_RESULTS
            """.trimIndent()
        }
    }

    @Test
    fun testRendersCorrectMessageWhenAtLeastExpected() {
        test(atLeast = 2, atMost = Int.MAX_VALUE, actualTracesCount = 1) {
            """
                Expected at least 2 calls, but 1 occurred for CALL_TEMPLATE!
                MATCHING_RESULTS
            """.trimIndent()
        }
    }

    @Test
    fun testRendersCorrectMessageWhenAtMostExpected() {
        test(atLeast = 1, atMost = 2, actualTracesCount = 3) {
            """
                Expected at most 2 calls, but 3 occurred for CALL_TEMPLATE!
                MATCHING_RESULTS
            """.trimIndent()
        }
    }

    @Test
    fun testRendersCorrectMessageWhenExactNumberOfCallsExpected() {
        test(atLeast = 2, atMost = 2, actualTracesCount = 1) {
            """
                Expected exactly 2 calls, but 1 occurred for CALL_TEMPLATE!
                MATCHING_RESULTS
            """.trimIndent()
        }
    }

    @Test
    fun testRendersCorrectMessageWhenAnyCallExpected() {
        test(atLeast = 1, atMost = Int.MAX_VALUE, actualTracesCount = 0) {
            """
                Expected any call, but no matching calls for CALL_TEMPLATE!
                MATCHING_RESULTS
            """.trimIndent()
        }
    }

    private inline fun test(
        atLeast: Int,
        atMost: Int,
        actualTracesCount: Int,
        crossinline expectedMessage: () -> String,
    ) {
        val results = TemplateGroupedMatchingResults(
            template = fakeCallTemplate(),
            calls = mapOf(CallMatchResult.Matching to List(actualTracesCount) { fakeCallTrace() })
        )
        val error = SoftVerifier.Error(
            expectedAtLeast = atLeast,
            expectedAtMost = atMost,
            templateMatchingResults = results
        )
        errorRenderer.assert(error) { expectedMessage() }
        assertEquals(results.template, templateRenderer.recordedCalls.single())
        assertEquals(results, matchingResultsRenderer.recordedCalls.single())
    }
}
