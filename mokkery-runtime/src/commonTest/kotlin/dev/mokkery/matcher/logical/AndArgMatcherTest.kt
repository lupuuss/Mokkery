package dev.mokkery.matcher.logical

import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.CaptureMatcher
import dev.mokkery.matcher.capture.asCapture
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AndArgMatcherTest {


    @Test
    fun testMatchesWhenAllMatchersSatisfied() {
        val matcher = LogicalMatchers.And(listOf(ArgMatcher.NotEqual(1), ArgMatcher.NotEqual(2)))
        assertTrue(matcher.matches(3))
    }

    @Test
    fun testComposedMatcherDoesNotMatchWhenAnyMatcherNotSatisfied() {
        val matcher = LogicalMatchers.And(listOf(ArgMatcher.NotEqual(1), ArgMatcher.NotEqual(2)))
        assertFalse(matcher.matches(1))
        assertFalse(matcher.matches(2))
    }

    @Test
    fun testPropagatesCapture() {
        val list1 = mutableListOf<Int>()
        val list2 = mutableListOf<Int>()
        val matcher1 = CaptureMatcher(list1.asCapture(), ArgMatcher.Any)
        val matcher2 = CaptureMatcher(list2.asCapture(), ArgMatcher.Any)
        LogicalMatchers.And(listOf(matcher2, matcher1))
            .apply {
                capture(1)
                capture(2)
            }
        assertEquals(listOf(1, 2), list1)
        assertEquals(listOf(1, 2), list2)
    }
}
