package dev.mokkery.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EqualsArgMatcherTest {

    private val matcher = ArgMatcher.Equals(1)

    @Test
    fun testReturnsTrueForSpecifiedValue() {
        assertTrue(matcher.matches(1))
    }

    @Test
    fun testReturnsFalseForOtherValues() {
        assertFalse(matcher.matches(0))
        assertFalse(matcher.matches(100))
        assertFalse(matcher.matches(-100))
    }

    @Test
    fun testToStringReturnsCorrectDescription() {
        assertEquals("1", matcher.toString())
    }
}
