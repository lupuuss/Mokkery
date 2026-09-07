package dev.mokkery.context

import dev.mokkery.test.TestContextElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class MemoizedContextTest {

    private val elementA = TestContextElement("a")
    private val elementB = TestContextElement("b")
    private val elementC = TestContextElement("c")

    private val memoized = MokkeryContext.memoized { +(elementA + elementB + elementC) }

    @Test
    fun testReturnsElementsFromOriginalContext() {
        memoized.assertContainsExactly(elementA, elementB, elementC)
    }

    @Test
    fun testMinusReturnsMemoizedWithoutSubtractedKey() {
        (memoized - elementA.key).assertContainsExactly(elementB, elementC)
    }

    @Test
    fun testMinusDoesNothingWhenNotExistingElement() {
        val initial = memoized
        val elementD = TestContextElement("d")
        val result = initial - elementD.key
        assertSame(initial, result)
        result.assertContainsExactly(elementA, elementB, elementC)
        assertNull(result[elementD.key])
    }

    @Test
    fun testToStringOfOriginalContext() {
        val expectedString = "[TestContextElement(value=a), TestContextElement(value=b), TestContextElement(value=c)]"
        assertEquals(expectedString, memoized.toString())
    }

    private val moduleHooks = Hooks("module")
    private val instanceHooks = Hooks("instance")

    private val fallback = MokkeryContext.memoized { +(elementA + elementB + moduleHooks) }
    private val layered = fallback.withMemoized {
        +instanceHooks
        +elementC
    }

    @Test
    fun testResolvesElementsFromBothLayers() {
        assertEquals(elementA, layered[elementA.key])
        assertEquals(elementC, layered[elementC.key])
    }

    @Test
    fun testElementShadowsFallbackElementWithSameKey() {
        assertEquals(instanceHooks, layered[Hooks.Key])
    }

    @Test
    fun testFoldYieldsEachKeyOnceWithShadowedElementReplaced() {
        val elements = layered.toList()
        assertEquals(4, elements.size)
        assertEquals(setOf(elementA, elementB, instanceHooks, elementC), elements.toSet())
    }

    @Test
    fun testMinusOfShadowedKeyRemovesItFromBothLayers() {
        assertNull((layered - Hooks.Key)[Hooks.Key])
    }

    @Test
    fun testMinusOfFallbackOnlyKeyRemovesItFromLayered() {
        val result = layered - elementA.key
        assertNull(result[elementA.key])
        assertEquals(setOf(elementB, instanceHooks, elementC), result.toList().toSet())
    }

    @Test
    fun testMinusOfUnknownKeyReturnsSameLayeredContext() {
        assertSame(layered, layered - TestContextElement("missing").key)
    }

    @Test
    fun testEmptyBuilderReturnsFallback() {
        assertSame(fallback, fallback.withMemoized { })
    }

    private data class Hooks(val tag: String) : MokkeryContext.Element {

        override val key: MokkeryContext.Key<*> get() = Key

        companion object Key : MokkeryContext.Key<Hooks>
    }
}
