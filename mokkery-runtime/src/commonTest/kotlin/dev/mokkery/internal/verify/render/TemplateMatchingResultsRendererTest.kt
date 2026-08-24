package dev.mokkery.internal.verify.render

import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.testRendering
import kotlin.test.Test

class TemplateMatchingResultsRendererTest {

    private val traceRenderer = TestRenderer<CallTrace>(MokkeryRendering.callEntryKey) { "RENDERER_TRACE" }
    private val templateRenderer = TestRenderer<CallTemplate>(MokkeryRendering.callTemplateKey) { "RENDERER_TEMPLATE" }
    private val context = traceRenderer + templateRenderer
    private val renderer = TemplateMatchingResultsRenderer

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
        testRendering(context) {
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
}
