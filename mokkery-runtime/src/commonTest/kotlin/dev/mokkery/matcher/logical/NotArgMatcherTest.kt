package dev.mokkery.matcher.logical

import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.CaptureMatcher
import dev.mokkery.matcher.capture.asCapture
import dev.mokkery.matcher.logical.LogicalMatchers.Not
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotArgMatcherTest {

    @Test
    fun testMatchesWhenMatcherIsNotSatisfied() {
        val matcher = Not(listOf(ArgMatcher.Equals(1)))
        assertTrue(matcher.matches(2))
    }

    @Test
    fun testComposedMatcherDoesNotMatchWhenMatcherIsSatisfied() {
        val matcher = Not(listOf(ArgMatcher.Equals(1)))
        assertFalse(matcher.matches(1))
    }

    @Test
    fun testPropagatesCapture() {
        val list = mutableListOf<Int>()
        Not(listOf(CaptureMatcher(list.asCapture(), ArgMatcher.Any))).apply {
            capture(1)
            capture(2)
        }
        assertEquals(listOf(1, 2), list)
    }
}
