package dev.mokkery.internal.verify.render

import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.NotVerifier
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

class NotVerifierErrorRendererTest {

    private val templateRenderer = TestRenderer<CallTemplate> { "CALL_TEMPLATE" }
    private val traceListRenderer = TestRenderer<List<CallTrace>> { "CALL_TRACES" }
    private val errorRenderer = NotVerifierErrorRenderer(
        templateRenderer = templateRenderer,
        traceListRenderer = traceListRenderer
    )

    @Test
    fun testRendersCorrectMessage() {
        val error = NotVerifier.Error(fakeCallTemplate(), listOf(fakeCallTrace()))
        errorRenderer.assert(error) {
            """
                Calls to CALL_TEMPLATE were not expected, but occurred:
                CALL_TRACES
            """.trimIndent()
        }
        assertEquals(error.template, templateRenderer.recordedCalls.single())
        assertEquals(error.unexpectedCalls, traceListRenderer.recordedCalls.single())
    }
}
