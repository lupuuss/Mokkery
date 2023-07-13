package dev.mokkery.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotEqualArgMatcherTest {

    private val matcher = ArgMatcher.NotEqual(1)

    @Test
    fun testReturnsFalseForSpecifiedValue() {
        assertFalse(matcher.matches(1))
    }

    @Test
    fun testReturnsTrueForOtherValues() {
        assertTrue(matcher.matches(0))
        assertTrue(matcher.matches(100))
        assertTrue(matcher.matches(-100))
    }

    @Test
    fun testToStringReturnsCorrectDescription() {
        assertEquals("notEq(1)", matcher.toString())
    }
}
