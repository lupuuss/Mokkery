package dev.mokkery.internal.verify.render

import dev.mokkery.internal.verify.ExhaustiveOrderVerifier
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.testRendering
import kotlin.test.Test

class ExhaustiveOrderVerifierErrorRendererTest {

    private val matchingResultsRenderer = TestRenderer<List<TemplateMatchingResult>>(
        VerifyRendering.templateMatchingResults
    ) { "MATCHING_RESULTS" }

    @Test
    fun testRendersCorrectMessage() {
        val results = listOf(TemplateMatchingResult.Matching(fakeCallTrace(), fakeCallTemplate()))
        testRendering(matchingResultsRenderer) {
            ExhaustiveOrderVerifierErrorRenderer.assert(ExhaustiveOrderVerifier.Error(results)) {
                """
                    Expected strict order of calls without unverified ones, but not satisfied!
                    MATCHING_RESULTS
                """.trimIndent()
            }
        }
    }
}
