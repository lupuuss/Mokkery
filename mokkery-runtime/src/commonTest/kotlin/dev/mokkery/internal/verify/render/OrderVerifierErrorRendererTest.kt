package dev.mokkery.internal.verify.render

import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.verify.OrderVerifier
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.testRendering
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderVerifierErrorRendererTest {

    private val templateRenderer = TestRenderer<CallTemplate>(MokkeryRendering.callTemplateKey) { "CALL_TEMPLATE" }
    private val matchingResultsRenderer = TestRenderer<List<TemplateMatchingResult>>(
        VerifyRendering.templateMatchingResults
    ) { "MATCHING_RESULTS" }
    private val context = templateRenderer + matchingResultsRenderer
    private val renderer = OrderVerifierErrorRenderer

    @Test
    fun testRendersCorrectMessage() {
        val error = OrderVerifier.Error(
            failedAt = fakeCallTemplate(),
            failedIndex = 1,
            results = listOf(
                TemplateMatchingResult.Matching(fakeCallTrace(id = 1), fakeCallTemplate(instanceId = 1)),
                TemplateMatchingResult.NoMatch(fakeCallTemplate(instanceId = 2))
            )
        )
        testRendering(context) {
            renderer.assert(error) {
                """
                    Expected calls in specified order but not satisfied! Failed at 2. CALL_TEMPLATE!
                    MATCHING_RESULTS
                """.trimIndent()
            }
        }
        assertEquals(error.failedAt, templateRenderer.recordedCalls.single())
        assertEquals(error.results, matchingResultsRenderer.recordedCalls.single())
    }
}
