package dev.mokkery.matcher.collections

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValueInIterableArgMatcherTest {

    private val matcher = CollectionArgMatchers.ValueInIterable(listOf(1, 2, 3))

    @Test
    fun testReturnsTrueForValuesThatAreInIterable() {
        assertTrue(matcher.matches(1))
        assertTrue(matcher.matches(2))
        assertTrue(matcher.matches(3))
    }

    @Test
    fun testReturnsFalseForOtherValuesThatAreInIterable() {
        assertFalse(matcher.matches(-1))
        assertFalse(matcher.matches(0))
        assertFalse(matcher.matches(4))
        assertFalse(matcher.matches(5))
    }
}
