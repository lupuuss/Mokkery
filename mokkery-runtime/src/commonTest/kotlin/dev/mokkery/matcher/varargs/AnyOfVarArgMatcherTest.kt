package dev.mokkery.matcher.varargs

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AnyOfVarArgMatcherTest {

    private val matcher = VarArgMatcher.AnyOf(Int::class)

    @Test
    fun testReturnsTrueForAnyArrayOrList() {
        assertTrue(matcher.matches(arrayOf(1, 2)))
        assertTrue(matcher.matches(listOf("")))
        assertTrue(matcher.matches(intArrayOf()))
    }

    @Test
    fun testToStringReturnsCorrectDescription() {
        assertEquals("anyVarargsInt()", matcher.toString())
    }
}
