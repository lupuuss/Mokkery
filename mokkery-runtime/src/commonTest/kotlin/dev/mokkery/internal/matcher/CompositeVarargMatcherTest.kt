package dev.mokkery.internal.matcher

import dev.mokkery.internal.rendering.MokkeryRendering
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.CaptureMatcher
import dev.mokkery.matcher.capture.asCapture
import dev.mokkery.matcher.collections.CollectionArgMatchers
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.testRendering
import kotlin.test.Test
import kotlin.test.assertContentEquals
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
    fun testRenderIsCorrectWithOnlyStartingValues() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
            )
        )
        assertEquals(
            expected = "[M(Equals(value=1)), M(Equals(value=2))]",
            actual = testRendering(argMatcherRenderer) { matcher.render() })
    }

    @Test
    fun testRenderIsCorrectWithStartingValuesAndWildcard() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
                ArgMatcher.Any.spread(),
            )
        )
        assertEquals(
            expected = "[M(Equals(value=1)), M(Equals(value=2)), M(SpreadArgMatcherImpl(matcher=Any))]",
            actual = testRendering(argMatcherRenderer) { matcher.render() }
        )
    }

    @Test
    fun testRenderIsCorrectWithStartingValuesEndingValuesAndWildcard() {
        val matcher = CompositeVarArgMatcher(
            listOf(
                ArgMatcher.Equals(1),
                ArgMatcher.Equals(2),
                ArgMatcher.Any.spread(),
                ArgMatcher.Equals(3),
                ArgMatcher.Equals(4),
            )
        )
        assertEquals(
            expected = "[M(Equals(value=1)), M(Equals(value=2)), M(SpreadArgMatcherImpl(matcher=Any)), M(Equals(value=3)), M(Equals(value=4))]",
            actual = testRendering(argMatcherRenderer) { matcher.render() }
        )
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

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testPropagatesCaptureToWildcard() {
        val captured = mutableListOf<Any?>()
        val matcher = CaptureMatcher(captured.asCapture(), ArgMatcher.Any)
        CompositeVarArgMatcher(listOf(matcher.spread() as ArgMatcher<Any?>)).apply {
            capture(intArrayOf(1, 2))
            capture(intArrayOf(3))
        }
        assertContentEquals(intArrayOf(1, 2), captured[0] as IntArray)
        assertContentEquals(intArrayOf(3), captured[1] as IntArray)
        assertEquals(2, captured.size)
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun testPropagatesCaptureToValuesAroundWildcard() {
        val before = mutableListOf<Int>()
        val wildcard = mutableListOf<Any?>()
        val after = mutableListOf<Int>()
        CompositeVarArgMatcher(
            listOf(
                CaptureMatcher(before.asCapture(), ArgMatcher.Any) as ArgMatcher<Any?>,
                CaptureMatcher(wildcard.asCapture(), ArgMatcher.Any).spread() as ArgMatcher<Any?>,
                CaptureMatcher(after.asCapture(), ArgMatcher.Any) as ArgMatcher<Any?>,
            )
        ).capture(intArrayOf(1, 2, 3, 4))
        assertEquals(listOf(1), before)
        assertEquals(listOf(4), after)
        assertContentEquals(intArrayOf(2, 3), wildcard.single() as IntArray)
    }

    private val argMatcherRenderer = TestRenderer<ArgMatcher<*>>(MokkeryRendering.argMatcherKey) { "M($it)" }
}
