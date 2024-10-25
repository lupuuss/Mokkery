package dev.mokkery.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnyArgMatcherTest {

    private val matcher = ArgMatcher.Any

    @Test
    fun testReturnsTrueForAnything() {
        assertTrue(matcher.matches(AnyArg))
        assertTrue(matcher.matches(null))
        assertTrue(matcher.matches(0))
    }

    @Test
    fun testToStringReturnsCorrectDescription() {
        assertEquals("any()", matcher.toString())
    }
}
