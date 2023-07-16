package dev.mokkery.matcher.varargs

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnyThatVarArgMatcherTest {

    private val matcher = VarArgMatcher.AnyThat<Long>(Long::class) { it == 2L }

    @Test
    fun returnsTrueIfAllElementsMatchThePredicate() {
        assertTrue(matcher.matches(longArrayOf(2, 3, 4, 5)))
    }

    @Test
    fun returnsFalseIfAnyElementDoesNotMatchThePredicate() {
        assertFalse(matcher.matches(longArrayOf(0, 1, 3, 4)))
    }
}
