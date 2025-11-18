package dev.mokkery.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("DEPRECATION")
class MatchingArgMatcherTest {

    private var capturedArg: String? = null
    private var predicateResult = false
    private var toStringResult = "customMatcher()"
    private val matcher = ArgMatcher.Matching<String>(
        toStringFun = { toStringResult },
        predicate = {
            capturedArg = it
            predicateResult
        },
    )

    @Test
    fun testCallsPredicateWithGivenArg() {
        matcher.matches("1")
        assertEquals("1", capturedArg)
        matcher.matches("2")
        assertEquals("2", capturedArg)
    }

    @Test
    fun testReturnsPredicateResultOnCall() {
        assertFalse(matcher.matches(""))
        predicateResult = true
        assertTrue(matcher.matches(""))
    }

    @Test
    fun testReturnsToStringFunResultOnToString() {
        assertEquals("customMatcher()", matcher.toString())
    }
}
