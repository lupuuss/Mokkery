package dev.mokkery.matcher.logical

import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.CaptureMatcher
import dev.mokkery.matcher.capture.asCapture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotArgMatcherTest {

    @Test
    fun testMatchesWhenMatcherIsNotSatisfied() {
        val matcher = LogicalMatchers.Not(ArgMatcher.Equals(1))
        assertTrue(matcher.matches(2))
    }

    @Test
    fun testComposedMatcherDoesNotMatchWhenMatcherIsSatisfied() {
        val matcher = LogicalMatchers.Not(ArgMatcher.Equals(1))
        assertFalse(matcher.matches(1))
    }

    @Test
    fun testPropagatesCapture() {
        val list = mutableListOf<Int>()
        LogicalMatchers.Not(CaptureMatcher(list.asCapture(), ArgMatcher.Any)).apply {
            capture(1)
            capture(2)
        }
        assertEquals(listOf(1, 2), list)
    }
}
