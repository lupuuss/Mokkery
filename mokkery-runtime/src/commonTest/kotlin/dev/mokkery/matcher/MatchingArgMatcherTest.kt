package dev.mokkery.matcher

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MatchingArgMatcherTest {

    private var predicateArg: String? = null
    private var predicateResult = false
    private var toStringResult = "customMatcher()"
    private val matcher = ArgMatcher.Matching<String>(
        toStringFun = { toStringResult },
        predicate = {
            predicateArg = it
            predicateResult
        },
    )

    @Test
    fun testCallsPredicateWithGivenArg() {
        matcher.matches("1")
        assertEquals("1", predicateArg)
        matcher.matches("2")
        assertEquals("2", predicateArg)
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
