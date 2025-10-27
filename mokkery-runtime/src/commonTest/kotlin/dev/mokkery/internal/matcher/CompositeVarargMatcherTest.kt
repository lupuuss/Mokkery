package dev.mokkery.internal.matcher

import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.CaptureMatcher
import dev.mokkery.matcher.capture.asCapture
import dev.mokkery.matcher.collections.CollectionArgMatchers
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompositeVarargMatcherTest {

    @Test
    fun testMatchesSequenceOfValuesWhenNoWildcard() {
        val matcher = CompositeVarArgMatcher(listOf(ArgMatcher.Equals(1), ArgMatcher.Equals(2)))
        assertTrue(matcher.matches(intArrayOf(1, 2)))
    }

    @Test
    fun testDoesNotMatchSequenceOfValuesWithAdditionalArgsWhenNoWildcard() {
        val matcher = CompositeVarArgMatcher(listOf(ArgMatcher.Equals(1), ArgMatcher.Equals(2)))
        assertFalse(matcher.matches(intArrayOf(1, 2, 2)))
        assertFalse(matcher.matches(intArrayOf(1, 1, 2)))
    }

    @Test
    fun testMatchesSequenceOfValuesWithoutAdditionalArgsWithWildcard() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
                ArgMatcher.Any.spread(),
            )
        )
        assertTrue(matcher.matches(intArrayOf(1, 2)))
    }

    @Test
    fun testMatchesSequenceOfValuesWithAdditionalArgsWhenWildcardMatchesAdditionalValues() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
                ArgMatcher.Any.spread(),
            )
        )
        assertTrue(matcher.matches(intArrayOf(1, 2, 3, 4, 5)))
    }

    @Test
    fun testDoesNotMatchSequenceOfValuesWithAdditionalArgsWhenWildcardDoesNotMatchAdditionalValues() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
                CollectionArgMatchers.ContainsAllArray<Int>(Int::class) { it == 1 }.spread(),
            )
        )
        assertFalse(matcher.matches(intArrayOf(1, 2, 3, 4, 5)))
    }

    @Test
    fun testDoesNotMatchSequenceOfValuesWithAdditionalArgsWhenStartingValuesDoesNotMatch() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
                ArgMatcher.Any.spread(),
            )
        )
        assertFalse(matcher.matches(intArrayOf(2, 2, 3, 4, 5)))
    }

    @Test
    fun testMatchesSequenceOfValuesAtStartAndAtTheEndWithWildcard() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
                ArgMatcher.Any.spread(),
                ArgMatcher.Equals(3),
                ArgMatcher.Equals(4),
            )
        )
        assertTrue(matcher.matches(intArrayOf(1, 2, 0, 0, 0, 3, 4)))
    }

    @Test
    fun testDoesNotMatchSequenceOfValuesAtStartAndAtTheEndWhenEndingDoesNotMatch() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
                ArgMatcher.Any.spread(),
                ArgMatcher.Equals(3),
                ArgMatcher.Equals(4),
            )
        )
        assertFalse(matcher.matches(intArrayOf(1, 2, 0, 0, 0, 3, 3)))
    }

    @Test
    fun testDoesNotMatchSequenceOfValuesAtStartAndAtTheEndWhenWildcardDoesNotMatch() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
                CollectionArgMatchers.ContainsAllArray<Int>(Int::class) { it == 1 }.spread(),
                ArgMatcher.Equals(3),
                ArgMatcher.Equals(4),
            )
        )
        assertFalse(matcher.matches(intArrayOf(1, 2, 0, 0, 0, 3, 4)))
    }

    @Test
    fun testDoesNotMatchSequenceOfValuesAtStartAndAtTheEndWhenStartDoesNotMatch() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
                CollectionArgMatchers.ContainsAllArray<Int>(Int::class) { it == 1 }.spread(),
                ArgMatcher.Equals(3),
                ArgMatcher.Equals(4),
            )
        )
        assertFalse(matcher.matches(intArrayOf(1, 1, 0, 0, 0, 3, 4)))
    }

    @Test
    fun testToStringIsCorrectWithOnlyStartingValues() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
            )
        )
        assertEquals("[1, 2]", matcher.toString())
    }

    @Test
    fun testToStringIsCorrectWithStartingValuesAndWildcard() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
                ArgMatcher.Any.spread(),
            )
        )
        assertEquals("[1, 2, *any()]", matcher.toString())
    }

    @Test
    fun testToStringIsCorrectWithStartingValuesEndingValuesAndWildcard() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
                ArgMatcher.Any.spread(),
                ArgMatcher.Equals(3),
                ArgMatcher.Equals(4),
            )
        )
        assertEquals("[1, 2, *any(), 3, 4]", matcher.toString())
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testPropagatesCapture() {
        val list1 = mutableListOf<Int>()
        val list2 = mutableListOf<Int>()
        val matcher1 = CaptureMatcher(list1.asCapture(), ArgMatcher.Any)
        val matcher2 = CaptureMatcher(list2.asCapture(), ArgMatcher.Any)
        CompositeVarArgMatcher(listOf(matcher1 as ArgMatcher<Any?>, matcher2 as ArgMatcher<Any?>)).apply {
            capture(intArrayOf(1, 3))
            capture(intArrayOf(2, 4))
        }
        assertEquals(listOf(1, 2), list1)
        assertEquals(listOf(3, 4), list2)
    }
}
