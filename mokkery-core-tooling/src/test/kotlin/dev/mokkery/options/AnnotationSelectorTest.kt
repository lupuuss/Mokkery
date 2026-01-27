package dev.mokkery.options

import dev.mokkery.options.AnnotationSelector.Companion.named
import dev.mokkery.options.AnnotationSelector.Companion.none
import dev.mokkery.options.AnnotationSelector.Companion.all
import dev.mokkery.options.AnnotationSelector.Companion.matches
import dev.mokkery.options.AnnotationSelectorInternals.Combined
import dev.mokkery.options.AnnotationSelectorInternals.Minus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AnnotationSelectorTest {

    @Test
    fun testNonePlusElementReturnsElement() {
        assertEquals(named("example.a"), none + named("example.a"))
    }

    @Test
    fun testUnaryMinusNoneIsNone() {
        assertEquals(none, -none)
    }

    @Test
    fun testDoubleNegatedAllIsAll() {
        assertEquals(all, -(-all))
    }

    @Test
    fun testNonePlusAllReturnsAll() {
        assertEquals(all, none + all)
    }

    @Test
    fun testNoneMinusElementReturnsNone() {
        assertEquals(-named("example.a"), none - named("example.a"))
    }

    @Test
    fun testAllPlusElementReturnsAll() {
        assertEquals(all, all + named("example.a"))
    }

    @Test
    fun testAllPlusNoneReturnsAll() {
        assertEquals(all, all + none)
    }

    @Test
    fun testAllMinusPlusElements() {
        assertEquals(all, all + none)
    }

    @Test
    fun testMultipleNamedAreMerged() {
        assertEquals(
            named("example.a", "example.b", "example.c"),
            named("example.a") + named("example.b") + named("example.c")
        )
    }

    @Test
    fun testMultipleMinusNamedAreMerged() {
        assertEquals(
            all - named("example.a", "example.b", "example.c"),
            all - named("example.a") - named("example.b") - named("example.c"),
        )
    }

    @Test
    fun testAllPlusManyIsAll() {
        assertEquals(
            all,
            all + named("example.a") + named("example.b") + named("example.c")
        )
    }

    @Test
    fun testManyPlusAllIsAll() {
        assertEquals(
            all,
            named("example.a") + named("example.b") + named("example.c") + all
        )
    }

    @Test
    fun testMinusPlusSameSelectorReturnsNone() {
        assertEquals(
            none,
            -named("example.a") + named("example.a")
        )
    }

    @Test
    fun testDoubleUnaryMinusReturnsOriginal() {
        assertEquals(
            named("example.a"),
            -(-named("example.a"))
        )
    }

    @Test
    fun testMinusPlusMinusMerges() {
        assertEquals(
            -(named("example.a", "example.b")),
            -named("example.a") + -named("example.b")
        )
    }

    @Test
    fun testNamedMinusSameNamedReturnsNone() {
        assertEquals(
            none,
            named("example.a", "example.b") - named("example.a", "example.b")
        )
    }

    @Test
    fun testNamedMinusSubsetReturnsReducedNamed() {
        assertEquals(
            named("example.a"),
            named("example.a", "example.b") - named("example.b")
        )
    }

    @Test
    fun testNamedMinusSupersetReturnsNegativeNamed() {
        assertEquals(
            -named("example.a"),
            named("example.b") - named("example.a", "example.b")
        )
    }

    @Test
    fun testMergesAdjacentCombinationsAndNamedWhenPlusIsUsed() {
        ((matches(Regex("com.+")) + named("example.a")) + (named("example.b") - matches(Regex("example.+"))))
            .assertCombined {
                assertEquals(matches(Regex("com.+")), elements[0])
                assertEquals(named("example.a", "example.b"), elements[1])
                assertEquals(-matches(Regex("example.+")), elements[2])
            }
    }

    @Test
    fun testMergesAdjacentCombinationsButNotNamedWhenTheyAreNotAdjacent() {
        (named("example.a") + named("example.b") + named("example.c") - matches(Regex("example.+")) + named("example.d"))
            .assertCombined {
                assertEquals(named("example.a", "example.b", "example.c"), elements[0])
                assertEquals(-matches(Regex("example.+")), elements[1])
                assertEquals(named("example.d"), elements[2])
            }
    }

    @Test
    fun testDoesNotMergeAdjacentCombinationsWhenMinusIsUsed() {
        (named("example.a", "example.b") - (named("example.b") + matches(Regex("example.+"))))
            .assertCombined {
                assertEquals(named("example.a", "example.b"), elements[0])
                assertIs<Minus>(elements[1]).selector.assertCombined {
                    assertEquals(named("example.b"), elements[0])
                    assertEquals(matches(Regex("example.+")), elements[1])
                }
            }
    }

    private fun AnnotationSelector.assertCombined(
        block: Combined.() -> Unit = { }
    ): Combined {
        return assertIs<Combined>(this).apply(block)
    }
}
