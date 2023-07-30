package dev.mokkery.matcher.capture

import dev.mokkery.internal.MissingMatchersForComposite
import dev.mokkery.matcher.ArgMatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CaptureMatcherTest {

    private val container = Capture.container<Int>()
    private val matcher = CaptureMatcher(container)

    @Test
    fun testFailsOnAssertFilledWhenMissingMatcher() {
        assertFailsWith<MissingMatchersForComposite> {
            matcher.assertFilled()
        }
    }

    @Test
    fun testComposedMatcherMatchesWhenMergedMatcherMatches() {
        val matcher = matcher.compose(ArgMatcher.Equals(1))
        assertTrue(matcher.matches(1))
        assertFalse(matcher.matches(2))
        assertFalse(matcher.matches(3))
    }

    @Test
    fun testComposedMatcherCapturesIntoCapture() {
        val matcher = matcher.compose(ArgMatcher.Equals(1)) as CaptureMatcher<Int>
        matcher.capture(1)
        matcher.capture(2)
        matcher.capture(3)
        assertEquals(listOf(1, 2, 3), container.values)
    }

    @Test
    fun testPropagatesCapture() {
        val list = mutableListOf<Int>()
        val matcher = CaptureMatcher(list.asCapture(), ArgMatcher.Any)
        this.matcher
            .compose(matcher)
            .apply {
                capture(1)
                capture(2)
            }
        assertEquals(listOf(1, 2), list)
    }
}
