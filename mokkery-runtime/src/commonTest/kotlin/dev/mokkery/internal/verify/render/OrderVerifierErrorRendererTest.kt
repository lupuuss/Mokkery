package dev.mokkery.internal.verify.render

import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.verify.OrderVerifier
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

class OrderVerifierErrorRendererTest {

    private val templateRenderer = TestRenderer<CallTemplate> { "CALL_TEMPLATE" }
    private val matchingResultsRenderer = TestRenderer<List<TemplateMatchingResult>> { "MATCHING_RESULTS" }
    private val errorRenderer = OrderVerifierErrorRenderer(templateRenderer, matchingResultsRenderer)

    @Test
    fun testRendersCorrectMessage() {
        val error = OrderVerifier.Error(
            failedAt = fakeCallTemplate(),
            failedIndex = 1,
            results = listOf(
                TemplateMatchingResult.Matching(fakeCallTrace(id = 1), fakeCallTemplate(id = 1)),
                TemplateMatchingResult.NoMatch(fakeCallTemplate(id = 2))
            )
        )
        errorRenderer.assert(error) {
            """
                Expected calls in specified order but not satisfied! Failed at 2. CALL_TEMPLATE!
                MATCHING_RESULTS
            """.trimIndent()
        }
        assertEquals(error.failedAt, templateRenderer.recordedCalls.single())
        assertEquals(error.results, matchingResultsRenderer.recordedCalls.single())
    }
}
