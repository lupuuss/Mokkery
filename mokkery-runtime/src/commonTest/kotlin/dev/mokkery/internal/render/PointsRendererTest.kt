package dev.mokkery.internal.render

import dev.mokkery.test.StubRenderer
import dev.mokkery.test.assert
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test

class PointsRendererTest {
    private val renderer = Renderers.default.points(
        point = "-",
        item = StubRenderer()
    )

    @Test
    fun testRendersPointList() {
        renderer.assert(listOf(fakeCallTrace(), fakeCallTrace())) {
            """
                - RENDERER_STUB
                - RENDERER_STUB
                
            """.trimIndent()
        }
    }
}
