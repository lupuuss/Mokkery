package dev.mokkery.matcher.logical

import dev.mokkery.internal.MissingMatchersForComposite
import dev.mokkery.matcher.ArgMatcher
import kotlin.test.Test
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
}
