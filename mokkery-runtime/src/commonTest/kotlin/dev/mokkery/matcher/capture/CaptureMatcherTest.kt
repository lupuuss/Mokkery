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
    fun testComposedMatcherCapturesWhenMatcherMatches() {
        val matcher = matcher.compose(ArgMatcher.Equals(1))
        matcher.matches(2)
        matcher.matches(1)
        matcher.matches(3)
        assertEquals(listOf(1), container.values)
    }
}
