package dev.mokkery.internal.verify.render

import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.testRendering
import kotlin.test.Test
import kotlin.test.assertEquals

class NoMoreCallsErrorRendererTest {

    private val instanceIdRenderer = TestRenderer<MokkeryInstanceId>(MokkeryRendering.instanceIdKey) { "INSTANCE_ID" }
    private val traceRenderer = TestRenderer<CallTrace>(MokkeryRendering.callTraceKey) { "CALL_TRACE" }
    private val context = instanceIdRenderer + traceRenderer + MokkeryRendering.Factory.Default
    private val renderer = NoMoreCallsErrorRenderer

    @Test
    fun testRendersCorrectMessage() {
        val id = MokkeryInstanceId("Foo", 1)
        val calls = listOf(fakeCallTrace())
        testRendering(context) {
            renderer.assert(id to calls) {
                """
                    Unverified calls for INSTANCE_ID:
                    * CALL_TRACE

                """.trimIndent()
            }
        }
        assertEquals(id, instanceIdRenderer.recordedCalls.single())
        assertEquals(calls, traceRenderer.recordedCalls)
    }
}
