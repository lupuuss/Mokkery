package dev.mokkery.internal.verify.render

import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

class NoMoreCallsErrorRendererTest {

    private val instanceIdRenderer = TestRenderer<MokkeryInstanceId> { "INSTANCE_ID" }
    private val callsListRenderer = TestRenderer<List<CallTrace>> { "CALLS" }

    private val errorRenderer = NoMoreCallsErrorRenderer(
        instanceIdRenderer = instanceIdRenderer,
        callsListRenderer = callsListRenderer,
    )

    @Test
    fun testRendersCorrectMessage() {
        val id = MokkeryInstanceId("Foo", 1)
        val calls = listOf(fakeCallTrace())
        errorRenderer.assert(id to calls) {
            """
                Unverified calls for INSTANCE_ID:
                CALLS
            """.trimIndent()
        }
        assertEquals(id, instanceIdRenderer.recordedCalls.single())
        assertEquals(calls, callsListRenderer.recordedCalls.single())
    }
}
