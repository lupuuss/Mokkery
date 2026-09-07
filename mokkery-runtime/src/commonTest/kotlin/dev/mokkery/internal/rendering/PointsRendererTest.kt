package dev.mokkery.internal.rendering

import dev.mokkery.test.StubRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.testRendering
import kotlin.test.Test

class PointsRendererTest {

    @Test
    fun testRendersPointList() {
        testRendering {
            val renderer = MokkeryRendering
                .Factory
                .Default
                .points(point = "-", item = StubRenderer())
            renderer.assert(listOf(fakeCallTrace(), fakeCallTrace())) {
                """
                - RENDERER_STUB
                - RENDERER_STUB

                """.trimIndent()
            }
        }
    }
}
