package dev.mokkery.matcher.varargs

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AllThatVarArgMatcherTest {

    private val matcher = VarArgMatcher.AllThat<Long>(Long::class) { it > 1 }

    @Test
    fun returnsTrueIfAllElementsMatchThePredicate() {
        assertTrue(matcher.matches(longArrayOf(2, 3, 4, 5)))
    }

    @Test
    fun returnsFalseIfAnyElementDoesNotMatchThePredicate() {
        assertFalse(matcher.matches(longArrayOf(2, 3, 4, 0)))
    }
}
