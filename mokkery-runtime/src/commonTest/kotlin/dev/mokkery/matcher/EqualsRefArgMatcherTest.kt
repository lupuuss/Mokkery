package dev.mokkery.matcher

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class EqualsRefArgMatcherTest {


    private val arg = DataClassArg(1)
    private val matcher = ArgMatcher.EqualsRef(arg)

    @Test
    fun testReturnsTrueOnlyForSpecifiedReference() {
        assertTrue(matcher.matches(arg))
    }

    @Test
    fun testReturnsFalseWhenEqualsToReturnsTrue() {
        assertFalse(matcher.matches(DataClassArg(1)))
    }

    @Test
    fun testReturnsFalseForOtherValues() {
        assertFalse(matcher.matches(DataClassArg(2)))
        assertFalse(matcher.matches(DataClassArg(3)))
        assertFalse(matcher.matches(DataClassArg(4)))
    }
}
