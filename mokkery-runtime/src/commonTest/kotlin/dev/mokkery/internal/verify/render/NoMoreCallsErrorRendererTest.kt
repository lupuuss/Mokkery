package dev.mokkery.internal.verify.render

import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.testRendering
import kotlin.test.Test
import kotlin.test.assertEquals

class NoMoreCallsErrorRendererTest {

    private val traceRenderer = TestRenderer<CallTrace>(MokkeryRendering.callEntryKey) { "CALL_TRACE" }
    private val context = traceRenderer + MokkeryRendering.Factory.Default
    private val renderer = NoMoreCallsErrorRenderer

    @Test
    fun testRendersCorrectMessage() {
        val calls = listOf(fakeCallTrace(name = "first"), fakeCallTrace(name = "second"))
        testRendering(context) {
            renderer.assert(calls) {
                """
                    No unverified calls expected, but these are present:
                    * CALL_TRACE
                    * CALL_TRACE

                """.trimIndent()
            }
        }
        assertEquals(calls, traceRenderer.recordedCalls)
    }
}
