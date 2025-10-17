package dev.mokkery.internal.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MaterializedDefaultMatcherTest {

    private val matcher = MaterializedDefaultValueMatcher(23)

    @Test
    fun testReturnsTrueWhenEqual() {
        assertTrue(matcher.matches(23))
    }

    @Test
    fun testReturnsFalseWhenNotEqual() {
        assertFalse(matcher.matches(22))
    }

    @Test
    fun testToStringReturnsExpectedValue() {
        assertEquals("default() => 23", matcher.toString())
    }
}
