package dev.mokkery.matcher

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OfTypeArgMatcherTest {
    private val matcher = ArgMatcher.OfType<Iterable<Int>>(List::class)

    @Test
    fun testMatchesWhenArgIsOfGivenType() {
        assertTrue(matcher.matches(listOf(1)))
        assertTrue(matcher.matches(listOf(2)))
    }

    @Test
    fun testDoesNotMatchWhenArgIsNotOfGivenType() {
        assertFalse(matcher.matches(setOf(3)))
        assertFalse(matcher.matches(setOf(3)))
    }
}
