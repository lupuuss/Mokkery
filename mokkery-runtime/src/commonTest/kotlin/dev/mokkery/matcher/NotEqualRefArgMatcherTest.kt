package dev.mokkery.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotEqualRefArgMatcherTest {

    private val arg = DataClassArg(1)
    private val matcher = ArgMatcher.NotEqualRef(arg)

    @Test
    fun testReturnsFalseOnlyForSpecifiedReference() {
        assertFalse(matcher.matches(arg))
    }

    @Test
    fun testReturnsTrueWhenEqualsToReturnsTrue() {
        assertTrue(matcher.matches(DataClassArg(1)))
    }

    @Test
    fun testReturnsTrueForOtherValues() {
        assertTrue(matcher.matches(DataClassArg(2)))
        assertTrue(matcher.matches(DataClassArg(3)))
        assertTrue(matcher.matches(DataClassArg(4)))
    }

    @Test
    fun testReturnsCorrectDescriptionOnToString() {
        assertEquals("neqRef(DataClassArg(value=1))", matcher.toString())
    }

}
