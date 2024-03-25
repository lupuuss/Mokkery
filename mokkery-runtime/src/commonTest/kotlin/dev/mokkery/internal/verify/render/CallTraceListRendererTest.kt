package dev.mokkery.internal.verify.render

import dev.mokkery.test.StubRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test

class CallTraceListRendererTest {
    private val renderer = CallTraceListRenderer(
        point = "-",
        traceRenderer = StubRenderer()
    )

    @Test
    fun testRendersCallTraceList() {
        renderer.assert(listOf(fakeCallTrace(), fakeCallTrace())) {
            """
                - RENDERER_STUB
                - RENDERER_STUB
            """.trimIndent()
        }
    }
}
