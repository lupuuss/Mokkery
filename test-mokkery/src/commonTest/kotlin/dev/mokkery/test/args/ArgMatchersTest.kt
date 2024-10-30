package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.getIfPresent
import dev.mokkery.matcher.capture.onArg
import dev.mokkery.matcher.collections.contentDeepEq
import dev.mokkery.matcher.collections.contentEq
import dev.mokkery.matcher.collections.isIn
import dev.mokkery.matcher.collections.isNotIn
import dev.mokkery.matcher.eq
import dev.mokkery.matcher.eqRef
import dev.mokkery.matcher.gte
import dev.mokkery.matcher.logical.and
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or
import dev.mokkery.matcher.lte
import dev.mokkery.matcher.neq
import dev.mokkery.matcher.neqRef
import dev.mokkery.matcher.nullable.notNull
import dev.mokkery.matcher.ofType
import dev.mokkery.mock
import dev.mokkery.test.ComplexArgsInterface
import dev.mokkery.test.ComplexType
import dev.mokkery.test.ComplexType.Companion.invoke
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArgMatchersTest {

    private val mock = mock<ComplexArgsInterface>()


    @Test
    fun testEqualityMatchers() {
        every { mock.callManyPrimitives(eq(1), neq(1.0)) } returns ComplexType
        assertEquals(ComplexType, mock.callManyPrimitives(1, 2.0))
        assertEquals(ComplexType, mock.callManyPrimitives(1, 3.0))
        assertFailsWith<MokkeryRuntimeException> { mock.callManyPrimitives(2, 2.0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callManyPrimitives(1, 1.0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callManyPrimitives(2, 1.0) }
    }

    @Test
    fun testRefEqualityMatchers() {
        val ref1 = ComplexType("a")
        val ref2 = ComplexType("a")
        every { mock.callComplex(neqRef(ref2)) } returns ComplexType
        every { mock.callComplex(eqRef(ref1)) } returns ref1
        assertEquals(ComplexType, mock.callComplex(ComplexType("a")))
        assertEquals(ref1, mock.callComplex(ref1))
        assertFailsWith<MokkeryRuntimeException> { mock.callComplex(ref2) }
    }

    @Test
    fun testComparisonMatchers() {
        every { mock.callPrimitive(lte(5)) } returns 5
        every { mock.callPrimitive(gte(7)) } returns 7
        assertEquals(5, mock.callPrimitive(5))
        assertEquals(5, mock.callPrimitive(4))
        assertEquals(7, mock.callPrimitive(7))
        assertEquals(7, mock.callPrimitive(8))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(6) }
    }

    @Test
    fun testOfTypeMatcher() {
        every { mock.callComplex(ofType<ComplexType.Companion>()) } returns ComplexType
        assertEquals(ComplexType, mock.callComplex(ComplexType))
        assertFailsWith<MokkeryRuntimeException> { mock.callComplex(ComplexType("a")) }
    }

    @Test
    fun testAndMatcher() {
        every { mock.callPrimitive(and(neq(1), neq(2))) } returns 1
        assertEquals(1, mock.callPrimitive(0))
        assertEquals(1, mock.callPrimitive(3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
    }

    @Test
    fun testOrMatcher() {
        every { mock.callPrimitive(or(eq(1), eq(2))) } returns 2
        assertEquals(2, mock.callPrimitive(1))
        assertEquals(2, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(3) }
    }

    @Test
    fun testNotMatcher() {
        every { mock.callComplex(not(eqRef(ComplexType))) } returns ComplexType
        assertEquals(ComplexType, mock.callComplex(mock()))
        assertFailsWith<MokkeryRuntimeException> { mock.callComplex(ComplexType) }
    }

    @Test
    fun testContentDeepEqForArray() {
        every { mock.callNestedArray(contentDeepEq(arrayOf(arrayOf(1), arrayOf(2)))) } returns arrayOf(1, 2)
        assertContentEquals(arrayOf(1, 2), mock.callNestedArray(arrayOf(arrayOf(1), arrayOf(2))))
        assertFailsWith<MokkeryRuntimeException> { mock.callNestedArray(arrayOf()) }
    }

    @Test
    fun testContentEqForArray() {
        every { mock.callArray(contentEq(arrayOf(ComplexType))) } returns arrayOf(ComplexType)
        assertContentEquals(arrayOf(ComplexType), mock.callArray(arrayOf(ComplexType)))
        assertFailsWith<MokkeryRuntimeException> {
            mock.callArray(arrayOf(ComplexType("a")))
        }
    }

    @Test
    fun testContentEqForIntArray() {
        every { mock.callIntArray(contentEq(intArrayOf(1, 2, 3))) } returns intArrayOf(6)
        assertContentEquals(intArrayOf(6), mock.callIntArray(intArrayOf(1, 2, 3)))
        assertFailsWith<MokkeryRuntimeException> {
            mock.callIntArray(intArrayOf(1, 2, 3, 4))
        }
    }

    @Test
    fun testIsIn() {
        every { mock.callPrimitive(isIn(1, 2, 3)) } returns 1
        every { mock.callPrimitive(isIn(listOf(5, 6))) } returns 2
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(0) }
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(1, mock.callPrimitive(2))
        assertEquals(1, mock.callPrimitive(3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(4) }
        assertEquals(2, mock.callPrimitive(5))
        assertEquals(2, mock.callPrimitive(6))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(7) }
    }

    @Test
    fun testIsNotIn() {
        every { mock.callPrimitive(isNotIn(1, 2)) } returns 1
        every { mock.callPrimitive(isNotIn(listOf(2, 3))) } returns 2
        assertEquals(2, mock.callPrimitive(0))
        assertEquals(2, mock.callPrimitive(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(2) }
        assertEquals(1, mock.callPrimitive(3))
        assertEquals(2, mock.callPrimitive(4))
    }

    @Test
    fun testCapture() {
        val slot = Capture.slot<Int>()
        every { mock.callManyPrimitives(capture(slot), 1.0) } returns ComplexType
        mock.callManyPrimitives(2, 1.0)
        mock.callManyPrimitives(3, 1.0)
        assertEquals(3, slot.getIfPresent())
    }

    @Test
    fun testCaptureWithNonDefaultMatcher() {
        val slot = Capture.slot<Int>()
        every { mock.callManyPrimitives(capture(slot, eq(2)), 1.0) } returns ComplexType
        mock.callManyPrimitives(2, 1.0)
        assertFailsWith<MokkeryRuntimeException> {
            mock.callManyPrimitives(3, 1.0)
        }
        assertEquals(2, slot.getIfPresent())
    }


    @Test
    fun testCaptureWithNotFullMatch() {
        val slot1 = Capture.slot<Int>()
        val slot2 = Capture.slot<Int>()
        every { mock.callManyPrimitives(capture(slot1), 1.0) } returns ComplexType
        every { mock.callManyPrimitives(capture(slot2), 2.0) } returns ComplexType
        mock.callManyPrimitives(2, 1.0)
        assertEquals(2, slot1.getIfPresent())
        assertEquals(null, slot2.getIfPresent())
    }

    @Test
    fun testCaptureCallbackIsCalled() {
        val called = mutableListOf<Int>()
        val callback = Capture.callback(callback = called::add)
        every { mock.callPrimitive(any()) } returns 1
        every { mock.callPrimitive(capture(callback, gte(1))) } returns 1
        mock.callPrimitive(0)
        mock.callPrimitive(1)
        mock.callPrimitive(3)
        assertEquals(listOf(1, 3), called)
    }

    @Test
    fun testOnArgCallbackIsCalled() {
        val called = mutableListOf<Int>()
        every { mock.callPrimitive(any()) } returns 1
        every { mock.callPrimitive(onArg(gte(1), called::add)) } returns 1
        mock.callPrimitive(0)
        mock.callPrimitive(1)
        mock.callPrimitive(3)
        assertEquals(listOf(1, 3), called)
    }

    @Test
    fun testNotNullMatcherAllowsUsingNotNullableMatchers() {
        every { mock.callNullable(any()) } returns 1
        every { mock.callNullable(notNull()) } returns 2
        every { mock.callNullable(notNull(gte(3))) } returns 3
        assertEquals(1, mock.callNullable(null))
        assertEquals(2, mock.callNullable(2))
        assertEquals(3, mock.callNullable(3))
    }
}

