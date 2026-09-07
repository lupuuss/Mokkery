package dev.mokkery.context

import dev.mokkery.test.TestContextElement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

private data class TopElement(val id: Int) : MokkeryContext.Element {

    override val key get() = Key

    companion object Key : MokkeryContext.Key<TopElement>
}

class KeepOnTopContextTest {

    private val elementA = TestContextElement("a")
    private val elementB = TestContextElement("b")
    private val top1 = TopElement(1)
    private val top2 = TopElement(2)

    @Test
    fun testReturnsTopElementForItsKey() {
        assertSame(top1, (elementA keepOnTop top1)[TopElement])
    }

    @Test
    fun testDelegatesOtherKeysToRest() {
        val context = elementA + elementB keepOnTop top1
        context.assertContainsExactly(elementA, elementB, top1)
        assertNull((elementA keepOnTop top1)[elementB.key])
    }

    @Test
    fun testKeepsTopLastWhenOtherElementsAreAdded() {
        val context = elementA keepOnTop top1
        (context + elementB).assertContainsExactly(elementA, elementB, top1)
    }

    @Test
    fun testReplacesTopWhenElementWithTheSameKeyIsAdded() {
        val context = elementA keepOnTop top1
        val result = context + elementB + top2

        assertSame(top2, result[TopElement])
        result.assertContainsExactly(elementA, elementB, top2)
    }

    @Test
    fun testReplacesTopWhenAddedContextIsNotAnElement() {
        val context = elementA keepOnTop top1
        val result = context + (elementB + top2)

        assertSame(top2, result[TopElement])
        result.assertContainsExactly(elementA, elementB, top2)
    }

    @Test
    fun testKeepOnTopRemovesElementWithTheSameKeyFromTheRest() {
        val result = elementA + top1 keepOnTop top2

        assertSame(top2, result[TopElement])
        result.assertContainsExactly(elementA, top2)
    }

    @Test
    fun testMinusTopKeyReturnsRest() {
        val rest = elementA + elementB
        assertSame(rest, (rest keepOnTop top1) - TopElement)
    }

    @Test
    fun testMinusRemovesElementFromRestAndKeepsTop() {
        val result = (elementA + elementB keepOnTop top1) - elementA.key

        assertNull(result[elementA.key])
        result.assertContainsExactly(elementB, top1)
    }

    @Test
    fun testMinusDoesNothingWhenNotExistingElement() {
        val context = elementA keepOnTop top1
        assertSame(context, context - elementB.key)
    }

    @Test
    fun testPlusEmptyReturnsTheSameContext() {
        val context = elementA keepOnTop top1
        assertSame(context, context + MokkeryContext.Empty)
    }

    @Test
    fun testToString() {
        assertEquals(
            "[TestContextElement(value=a), TopElement(id=1)]",
            (elementA keepOnTop top1).toString()
        )
    }
}
