package dev.mokkery.internal.matcher

import dev.mokkery.internal.MultipleVarargGenericMatchersException
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.varargs.VarArgMatcher
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompositeVarargMatcherTest {

    private val matcher = CompositeVarArgMatcher(Int::class)

    @Test
    fun testComposeFailsOnMultipleVarArgMatchers() {
        assertFailsWith<MultipleVarargGenericMatchersException> {
            matcher
                .compose(VarArgMatcher.AnyOf(Int::class))
                .compose(VarArgMatcher.AnyOf(Int::class))
        }
    }

    @Test
    fun testComposeAccumulatesMatchersProperly() {
        val result = matcher
            .compose(ArgMatcher.Equals(4))
            .compose(ArgMatcher.Equals(3))
            .compose(VarArgMatcher.AnyOf(Int::class))
            .compose(ArgMatcher.Equals(2))
            .compose(ArgMatcher.Equals(1))
        val expectedMatchers = listOf<ArgMatcher<Any?>>(
            ArgMatcher.Equals(1),
            ArgMatcher.Equals(2),
            VarArgMatcher.AnyOf(Int::class),
            ArgMatcher.Equals(3),
            ArgMatcher.Equals(4),
        )
        assertEquals(CompositeVarArgMatcher(Int::class, expectedMatchers), result)
    }

    @Test
    fun testMatchesSequenceOfValuesWhenNoWildcard() {
        val matcher = matcher.compose(ArgMatcher.Equals(2)).compose(ArgMatcher.Equals(1))
        assertTrue(matcher.matches(intArrayOf(1, 2)))
    }

    @Test
    fun testDoesNotMatchSequenceOfValuesWithAdditionalArgsWhenNoWildcard() {
        val matcher = matcher.compose(ArgMatcher.Equals(2)).compose(ArgMatcher.Equals(1))
        assertFalse(matcher.matches(intArrayOf(1, 2, 2)))
        assertFalse(matcher.matches(intArrayOf(1, 1, 2)))
    }

    @Test
    fun testMatchesSequenceOfValuesWithoutAdditionalArgsWithWildcard() {
        val matcher = matcher
            .compose(VarArgMatcher.AnyOf(Int::class))
            .compose(ArgMatcher.Equals(2))
            .compose(ArgMatcher.Equals(1))
        assertTrue(matcher.matches(intArrayOf(1, 2)))
    }

    @Test
    fun testMatchesSequenceOfValuesWithAdditionalArgsWhenWildcardMatchesAdditionalValues() {
        val matcher = matcher
            .compose(VarArgMatcher.AnyOf(Int::class))
            .compose(ArgMatcher.Equals(2))
            .compose(ArgMatcher.Equals(1))
        assertTrue(matcher.matches(intArrayOf(1, 2, 3, 4, 5)))
    }

    @Test
    fun testDoesNotMatchSequenceOfValuesWithAdditionalArgsWhenWildcardDoesNotMatchAdditionalValues() {
        val matcher = matcher
            .compose(VarArgMatcher.AllThat(Int::class) { it == 1 })
            .compose(ArgMatcher.Equals(2))
            .compose(ArgMatcher.Equals(1))
        assertFalse(matcher.matches(intArrayOf(1, 2, 3, 4, 5)))
    }

    @Test
    fun testDoesNotMatchSequenceOfValuesWithAdditionalArgsWhenStartingValuesDoesNotMatch() {
        val matcher = matcher
            .compose(VarArgMatcher.AnyOf(Int::class))
            .compose(ArgMatcher.Equals(2))
            .compose(ArgMatcher.Equals(1))
        assertFalse(matcher.matches(intArrayOf(2, 2, 3, 4, 5)))
    }

    @Test
    fun testMatchesSequenceOfValuesAtStartAndAtTheEndWithWildcard() {
        val matcher = matcher
            .compose(ArgMatcher.Equals(4))
            .compose(ArgMatcher.Equals(3))
            .compose(VarArgMatcher.AnyOf(Int::class))
            .compose(ArgMatcher.Equals(2))
            .compose(ArgMatcher.Equals(1))
        assertTrue(matcher.matches(intArrayOf(1, 2, 0, 0, 0, 3, 4)))
    }

    @Test
    fun testDoesNotMatchSequenceOfValuesAtStartAndAtTheEndWhenEndingDoesNotMatch() {
        val matcher = matcher
            .compose(ArgMatcher.Equals(4))
            .compose(ArgMatcher.Equals(3))
            .compose(VarArgMatcher.AnyOf(Int::class))
            .compose(ArgMatcher.Equals(2))
            .compose(ArgMatcher.Equals(1))
        assertFalse(matcher.matches(intArrayOf(1, 2, 0, 0, 0, 3, 3)))
    }

    @Test
    fun testDoesNotMatchSequenceOfValuesAtStartAndAtTheEndWhenWildcardDoesNotMatch() {
        val matcher = matcher
            .compose(ArgMatcher.Equals(4))
            .compose(ArgMatcher.Equals(3))
            .compose(VarArgMatcher.AllThat(Int::class) { it == 1 })
            .compose(ArgMatcher.Equals(2))
            .compose(ArgMatcher.Equals(1))
        assertFalse(matcher.matches(intArrayOf(1, 2, 0, 0, 0, 3, 4)))
    }

    @Test
    fun testDoesNotMatchSequenceOfValuesAtStartAndAtTheEndWhenStartDoesNotMatch() {
        val matcher = matcher
            .compose(ArgMatcher.Equals(4))
            .compose(ArgMatcher.Equals(3))
            .compose(VarArgMatcher.AllThat(Int::class) { it == 1 })
            .compose(ArgMatcher.Equals(2))
            .compose(ArgMatcher.Equals(1))
        assertFalse(matcher.matches(intArrayOf(1, 1, 0, 0, 0, 3, 4)))
    }
}
