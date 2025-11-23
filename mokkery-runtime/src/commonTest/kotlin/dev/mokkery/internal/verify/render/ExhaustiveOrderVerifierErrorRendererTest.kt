package dev.mokkery.internal.verify.render

import dev.mokkery.internal.verify.ExhaustiveOrderVerifier
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test

class ExhaustiveOrderVerifierErrorRendererTest {

    private val matchingResultsRenderer = TestRenderer<List<TemplateMatchingResult>> { "MATCHING_RESULTS" }
    private val errorRenderer = ExhaustiveOrderVerifierErrorRenderer(
        matchingResultsRenderer = matchingResultsRenderer
    )

    @Test
    fun testRendersCorrectMessage() {
        val results = listOf(TemplateMatchingResult.Matching(fakeCallTrace(), fakeCallTemplate()))
        errorRenderer.assert(ExhaustiveOrderVerifier.Error(results)) {
            """
                Expected strict order of calls without unverified ones, but not satisfied!
                MATCHING_RESULTS
            """.trimIndent()
        }
    }
}
