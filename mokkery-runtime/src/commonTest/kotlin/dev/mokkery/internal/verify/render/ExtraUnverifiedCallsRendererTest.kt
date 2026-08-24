package dev.mokkery.internal.verify.render

import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.testRendering
import kotlin.test.Test

class ExtraUnverifiedCallsRendererTest {

    private val traceRenderer = TestRenderer<CallTrace>(MokkeryRendering.callEntryKey) { "CALL_TRACE" }
    private val context = traceRenderer + MokkeryRendering.Factory.Default
    private val renderer = ExtraUnverifiedCallsRenderer

    @Test
    fun testRendersUnverifiedCalls() {
        testRendering(context) {
            renderer.assert(listOf(fakeCallTrace(), fakeCallTrace())) {
                """
                    All expected calls have been satisfied! However, there should not be any unverified calls, yet these are present:
                    * CALL_TRACE
                    * CALL_TRACE

                """.trimIndent()
            }
        }
    }
}
