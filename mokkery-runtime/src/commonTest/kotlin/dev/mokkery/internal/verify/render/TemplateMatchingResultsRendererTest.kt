package dev.mokkery.internal.verify.render

import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.test.StubRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test

class TemplateMatchingResultsRendererTest {

    private val renderer = TemplateMatchingResultsRenderer(
        StubRenderer("TRACE"),
        StubRenderer("TEMPLATE")
    )
    private val results = listOf(
        TemplateMatchingResult.UnverifiedCall(fakeCallTrace()),
        TemplateMatchingResult.Matching(fakeCallTrace(), fakeCallTemplate()),
        TemplateMatchingResult.Matching(fakeCallTrace(), fakeCallTemplate()),
        TemplateMatchingResult.UnverifiedCall(fakeCallTrace()),
        TemplateMatchingResult.UnverifiedCall(fakeCallTrace()),
        TemplateMatchingResult.NoMatch(fakeCallTemplate()),
        TemplateMatchingResult.UnverifiedCall(fakeCallTrace()),
    )

    @Test
    fun testAllTypesOfResultsWithCorrectIndexingColumn() {
        renderer.assert(results) {
            """
                Expected calls with matches (x.) and unverified calls (*) in order:
                *    RENDERER_TRACE
                1. ┌ RENDERER_TEMPLATE
                   └ RENDERER_TRACE
                2. ┌ RENDERER_TEMPLATE
                   └ RENDERER_TRACE
                *    RENDERER_TRACE
                *    RENDERER_TRACE
                3. ┌ RENDERER_TEMPLATE
                   └ No matching call!
                *    RENDERER_TRACE
                
            """.trimIndent()
        }
    }
}
