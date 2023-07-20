package dev.mokkery.matcher.capture

import kotlin.test.Test
import kotlin.test.assertEquals

class CaptureContainerTest {

    private val container = Capture.container<Int>()

    @Test
    fun testCapturesAllValues() {
        container.capture(1)
        container.capture(2)
        container.capture(3)
        assertEquals(listOf(1, 2, 3), container.values)
    }
}
