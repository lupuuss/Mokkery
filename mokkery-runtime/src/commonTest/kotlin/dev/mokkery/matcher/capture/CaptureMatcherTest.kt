package dev.mokkery.matcher.capture


import dev.mokkery.matcher.ArgMatcher
import kotlin.test.Test
import kotlin.test.assertEquals

class CaptureMatcherTest {

    @Test
    fun testCapturesIntoCapture() {
        val container = Capture.container<Int>()
        val matcher = CaptureMatcher(container, ArgMatcher.Equals(1))
        matcher.capture(1)
        matcher.capture(2)
        matcher.capture(3)
        assertEquals(listOf(1, 2, 3), container.values)
    }

    @Test
    fun testPropagatesCapture() {
        val list = mutableListOf<Int>()
        CaptureMatcher(Capture.void(), CaptureMatcher(list.asCapture(), ArgMatcher.Any)).apply {
            capture(1)
            capture(2)
        }
        assertEquals(listOf(1, 2), list)
    }
}
