package dev.mokkery.internal.verify.render

import dev.mokkery.test.StubRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test

class UnverifiedCallsRendererTest {

    private val renderer = UnverifiedCallsRenderer(StubRenderer("TRACE_LIST"))

    @Test
    fun testRendersUnverifiedCalls() {
        renderer.assert(listOf(fakeCallTrace(), fakeCallTrace())) {
            """
                All expected calls have been satisfied! However, there should not be any unverified calls, yet these are present:
                RENDERER_TRACE_LIST
            """.trimIndent()
        }
    }
}
