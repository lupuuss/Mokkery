package dev.mokkery.matcher.varargs

import dev.mokkery.matcher.AnyArg
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnyOfVarargsMatcherTest {

    private val matcher = VarArgMatcher.AnyOf(Int::class)

    @Test
    fun testReturnsTrueForAnything() {
        assertTrue(matcher.matches(AnyArg))
        assertTrue(matcher.matches(null))
        assertTrue(matcher.matches(0))
    }

    @Test
    fun testToStringReturnsCorrectDescription() {
        assertEquals("anyVarargsInt()", matcher.toString())
    }
}
