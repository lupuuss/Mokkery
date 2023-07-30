package dev.mokkery.matcher.logical

import dev.mokkery.internal.MissingMatchersForComposite
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.CaptureMatcher
import dev.mokkery.matcher.capture.asCapture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotArgMatcherTest {
    private val matcher = LogicalMatchers.Not<Int>()

    @Test
    fun testValidationFailsWhenMissingMatcher() {
        assertFailsWith<MissingMatchersForComposite> {
            matcher.assertFilled()
        }
    }

    @Test
    fun testValidationPassesWhenProperlyFilled() {
        matcher
            .compose(ArgMatcher.Equals(1))
            .assertFilled()
    }

    @Test
    fun testIsFilledReturnsFalseWhenMissingMatcher() {
        assertFalse(matcher.isFilled())
    }

    @Test
    fun testIsFilledReturnsTrueWhenFilled() {
        assertTrue(matcher.compose(ArgMatcher.Equals(2)).isFilled())
    }

    @Test
    fun testComposedMatcherMatchesWhenMatcherIsNotSatisfied() {
        val matcher = matcher.compose(ArgMatcher.Equals(1))
        assertTrue(matcher.matches(2))
    }

    @Test
    fun testComposedMatcherDoesNotMatchWhenMatcherIsSatisfied() {
        val matcher = matcher.compose(ArgMatcher.Equals(1))
        assertFalse(matcher.matches(1))
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
