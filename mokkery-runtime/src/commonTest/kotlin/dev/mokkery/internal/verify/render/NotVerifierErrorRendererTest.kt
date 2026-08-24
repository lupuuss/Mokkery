package dev.mokkery.internal.verify.render

import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.NotVerifier
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.testRendering
import kotlin.test.Test
import kotlin.test.assertEquals

class NotVerifierErrorRendererTest {

    private val templateRenderer = TestRenderer<CallTemplate>(MokkeryRendering.callTemplateKey) { "CALL_TEMPLATE" }
    private val traceRenderer = TestRenderer<CallTrace>(MokkeryRendering.callEntryKey) { "CALL_TRACE" }
    private val context = templateRenderer + traceRenderer + MokkeryRendering.Factory.Default
    private val renderer = NotVerifierErrorRenderer

    @Test
    fun testRendersCorrectMessage() {
        val error = NotVerifier.Error(fakeCallTemplate(), listOf(fakeCallTrace()))
        testRendering(context) {
            renderer.assert(error) {
                """
                    Calls to CALL_TEMPLATE were not expected, but occurred:
                    * CALL_TRACE

                """.trimIndent()
            }
        }
        assertEquals(error.template, templateRenderer.recordedCalls.single())
        assertEquals(error.unexpectedCalls, traceRenderer.recordedCalls)
    }
}
