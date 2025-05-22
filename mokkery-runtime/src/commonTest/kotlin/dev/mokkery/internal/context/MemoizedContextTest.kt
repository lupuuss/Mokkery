package dev.mokkery.internal.context

import dev.mokkery.test.TestContextElement
import dev.mokkery.test.assertContainsExactly
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

class MemoizedContextTest {

    private val elementA = TestContextElement("a")
    private val elementB = TestContextElement("b")
    private val elementC = TestContextElement("c")

    private val memoized = (elementA + elementB + elementC).memoized()

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
}
