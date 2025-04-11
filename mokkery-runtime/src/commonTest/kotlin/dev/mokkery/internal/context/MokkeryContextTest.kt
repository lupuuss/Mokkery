package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.forEach
import dev.mokkery.context.require
import dev.mokkery.test.TestContextElement
import dev.mokkery.test.assertContainsExactly
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNull
import kotlin.test.assertSame

class MokkeryContextTest {

    private val elementA = TestContextElement("a")
    private val elementB = TestContextElement("b")
    private val elementC = TestContextElement("c")

    @Test
    fun testEmptyPlusElementReturnsElement() {
        val result = MokkeryContext.Empty + elementA
        assertEquals(elementA, result)
    }

    @Test
    fun testPlusNewContextWithLeftAndRightContexts() {
        val result = elementA + elementB + elementC
        result.assertContainsExactly(elementA, elementB, elementC)
    }

    @Test
    fun testMinusReturnsNewContextWithoutElementWithSpecifiedKey() {
        var result = elementA + elementB + elementC - elementA.key

        result.assertContainsExactly(elementB, elementC)
        assertNull(result[elementA.key])

        result -= elementC.key
        result.assertContainsExactly(elementB)
        assertNull(result[elementA.key])
        assertNull(result[elementC.key])
    }

    @Test
    fun testMinusDoesNothingWhenNotExistingElement() {
        val initial = elementA + elementB
        val result = initial - elementC.key
        assertSame(initial, result)
        result.assertContainsExactly(elementA, elementB)
        assertNull(result[elementC.key])
    }

    @Test
    fun testElementMinusElementReturnsEmptyContext() {
        assertEquals(MokkeryContext.Empty, elementA - elementA.key)
    }

    @Test
    fun testFoldsCombinedElements() {
        val result = elementA + elementB + elementC
        val foldResult = result.fold(listOf<MokkeryContext.Element>()) { acc, it -> acc + it }
        assertEquals(listOf(elementA, elementB, elementC), foldResult)
    }

    @Test
    fun testElementFoldUsesOnlyElement() {
        val foldResult = elementA.fold(listOf<MokkeryContext.Element>()) { acc, it -> acc + it }
        assertEquals(listOf(elementA), foldResult)
    }

    @Test
    fun testEmptyContextFoldReturnsInitialValue() {
        val foldResult = MokkeryContext.Empty.fold(listOf<MokkeryContext.Element>()) { acc, it -> acc + it }
        assertEquals(listOf(), foldResult)
    }

    @Test
    fun testForEach() {
        val result = elementA + elementB + elementC
        val forEachResult = mutableListOf<MokkeryContext.Element>()
        result.forEach(forEachResult::add)
        assertEquals(listOf<MokkeryContext.Element>(elementA, elementB, elementC), forEachResult)
    }


    @Test
    fun testRequireWhenExistingElement() {
        assertEquals(elementA, (elementA + elementB + elementC).require(elementA.key))
    }

    @Test
    fun testRequireThrowsOnMissingElement() {
        assertFails { (elementA + elementB).require(elementC.key) }
    }

    @Test
    fun testCombinedContextToString() {
        assertEquals("[TestContextElement(value=a), TestContextElement(value=b)]", (elementA + elementB).toString())
    }
}
