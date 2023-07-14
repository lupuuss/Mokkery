package dev.mokkery.matcher.collections

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValueNotInIterableArgMatcherTest {
    private val matcher = CollectionArgMatchers.ValueNotInIterable(listOf(1, 2, 3))

    @Test
    fun testReturnsTrueForValuesThatAreInIterable() {
        assertFalse(matcher.matches(1))
        assertFalse(matcher.matches(2))
        assertFalse(matcher.matches(3))
    }

    @Test
    fun testReturnsFalseForOtherValuesThatAreInIterable() {
        assertTrue(matcher.matches(-1))
        assertTrue(matcher.matches(0))
        assertTrue(matcher.matches(4))
        assertTrue(matcher.matches(5))
    }

    @Test
    fun testToStringReturnsCorrectDescription() {
        assertEquals("isNotIn(1, 2, 3)", matcher.toString())
    }
}
