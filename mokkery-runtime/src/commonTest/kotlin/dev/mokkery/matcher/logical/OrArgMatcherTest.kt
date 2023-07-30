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

class OrArgMatcherTest {

    private val matcher = LogicalMatchers.Or<Int>(2)

    @Test
    fun testValidationFailsWhenMissingMatchers() {
        assertFailsWith<MissingMatchersForComposite> {
            matcher.compose(ArgMatcher.Equals(1)).assertFilled()
        }
    }

    @Test
    fun testValidationPassesWhenProperlyFilled() {
        matcher
            .compose(ArgMatcher.Equals(1))
            .compose(ArgMatcher.Equals(2))
            .assertFilled()
    }

    @Test
    fun testIsFilledReturnsFalseWhenMissingMatchers() {
        assertFalse(matcher.isFilled())
        assertFalse(matcher.compose(ArgMatcher.Equals(1)).isFilled())
    }

    @Test
    fun testIsFilledReturnsTrueWhenFilled() {
        assertTrue(matcher.compose(ArgMatcher.Equals(1)).compose(ArgMatcher.Equals(2)).isFilled())
    }

    @Test
    fun testComposedMatcherMatchesWhenAnyMatchersSatisfied() {
        val matcher = matcher
            .compose(ArgMatcher.Equals(1))
            .compose(ArgMatcher.Equals(2))
        assertTrue(matcher.matches(1))
        assertTrue(matcher.matches(2))
    }

    @Test
    fun testComposedMatcherDoesNotMatchWhenAnyMatcherNotSatisfied() {
        val matcher = matcher
            .compose(ArgMatcher.Equals(0))
            .compose(ArgMatcher.Equals(1))
        assertFalse(matcher.matches(-1))
        assertFalse(matcher.matches(2))
    }

    @Test
    fun testPropagatesCapture() {
        val list1 = mutableListOf<Int>()
        val list2 = mutableListOf<Int>()
        val matcher1 = CaptureMatcher(list1.asCapture(), ArgMatcher.Any)
        val matcher2 = CaptureMatcher(list2.asCapture(), ArgMatcher.Any)
        matcher
            .compose(matcher2)
            .compose(matcher1)
            .apply {
                capture(1)
                capture(2)
            }
        assertEquals(listOf(1, 2), list1)
        assertEquals(listOf(1, 2), list2)
    }
}
