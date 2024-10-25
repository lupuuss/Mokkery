package dev.mokkery.matcher.nullable

import dev.mokkery.internal.MissingMatchersForComposite
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.CaptureMatcher
import dev.mokkery.matcher.capture.asCapture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotNullMatcherTest {

    private val matcher = NotNullMatcher<Int?>(null)

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
    fun testComposedMatcherMatchesWhenArgumentIsNotNullAndMatchingMatcher() {
        val matcher = matcher.compose(ArgMatcher.Equals(1))
        assertTrue(matcher.matches(1))
    }

    @Test
    fun testComposedMatcherDoesNotMatchWhenArgumentIsNull() {
        val matcher = matcher.compose(ArgMatcher.Equals(1))
        assertFalse(matcher.matches(null))
    }

    @Test
    fun testComposedMatcherDoesNotMatchWhenArgumentIsNotNullButMatcherNotSatisfied() {
        val matcher = matcher.compose(ArgMatcher.Equals(1))
        assertFalse(matcher.matches(2))
    }

    @Test
    fun testPropagatesCaptureOnlyWithNotNullArgument() {
        val list = mutableListOf<Int>()
        val matcher = CaptureMatcher(list.asCapture(), ArgMatcher.Any)
        this.matcher
            .compose(matcher.unsafeCast())
            .apply {
                capture(1)
                capture(null)
                capture(2)
                capture(null)
            }
        assertEquals(listOf(1, 2), list)
    }
}
