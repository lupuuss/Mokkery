package dev.mokkery.test

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
import dev.mokkery.matcher.gt
import dev.mokkery.matcher.gte
import dev.mokkery.matcher.logical.and
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or
import dev.mokkery.matcher.lt
import dev.mokkery.matcher.lte
import dev.mokkery.matcher.neq
import dev.mokkery.matcher.neqRef
import dev.mokkery.matcher.ofType
import dev.mokkery.matcher.varargs.anyVarargs
import dev.mokkery.matcher.varargs.varargsAll
import dev.mokkery.matcher.varargs.varargsAny
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArgMatchersTest {

    private val mock = mock<TestInterface>()


    @Test
    fun testEqualityMatchers() {
        every { mock.callWithPrimitives(eq(1), neq(1)) } returns 1.0
        assertEquals(1.0, mock.callWithPrimitives(1, 2))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithPrimitives(2, 1) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithPrimitives(1, 1) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithPrimitives(2, 2) }
    }

    @Test
    fun testRefEqualityMatchers() {
        val ref1 = listOf("1")
        val ref2 = listOf("1")
        every { mock.callWithComplex(neqRef(ref2)) } returns listOf(2)
        every { mock.callWithComplex(eqRef(ref1)) } returns listOf(1)
        assertEquals(listOf(1), mock.callWithComplex(ref1))
        assertEquals(listOf(2), mock.callWithComplex(listOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithComplex(ref2) }
    }

    @Test
    fun testComparisonMatchers() {
        every { mock.callWithPrimitives(lte(0), gte(2)) } returns 2.0
        every { mock.callWithPrimitives(lt(0), gt(2)) } returns 1.0
        assertEquals(2.0, mock.callWithPrimitives(0, 2))
        assertEquals(1.0, mock.callWithPrimitives(-1, 3))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithPrimitives(1, 1) }
    }

    @Test
    fun testOfTypeMatcher() {
        every { mock.callWithComplex(ofType<ArrayList<String>>()) } returns listOf(1)
        assertEquals(listOf(1), mock.callWithComplex(arrayListOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithComplex(ArrayDeque()) }
    }

    @Test
    fun testAndMatcher() {
        every { mock.callWithPrimitives(and(neq(1), neq(2)), and(neq(3), neq(4))) } returns 2.0
        assertEquals(2.0, mock.callWithPrimitives(3, 2))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithPrimitives(2, 2) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithPrimitives(1, 2) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithPrimitives(3, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithPrimitives(3, 4) }
    }

    @Test
    fun testOrMatcher() {
        every { mock.callWithPrimitives(or(eq(1), eq(2)), or(eq(3), eq(4))) } returns 2.0
        assertEquals(2.0, mock.callWithPrimitives(1, 3))
        assertEquals(2.0, mock.callWithPrimitives(2, 3))
        assertEquals(2.0, mock.callWithPrimitives(1, 4))
        assertEquals(2.0, mock.callWithPrimitives(2, 4))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithPrimitives(0, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithPrimitives(1, 0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithPrimitives(0, 0) }
    }

    @Test
    fun testNotMatcher() {
        every { mock.callWithSelf(not(eqRef(mock))) } returns Unit
        mock.callWithSelf(mock())
        assertFailsWith<MokkeryRuntimeException> { mock.callWithSelf(mock) }
    }

    @Test
    fun testCompositeMatchersCombining() {
        every {
            mock.callWithPrimitives(
                i = not(and(neq(1), neq(2))),
                j = or(and(neq(1), neq(2)), and(neq(3), neq(4)))
            )
        } returns 1.0
        assertEquals(1.0, mock.callWithPrimitives(1, 3))
        assertEquals(1.0, mock.callWithPrimitives(2, 3))
        assertEquals(1.0, mock.callWithPrimitives(1, 1))
        assertEquals(1.0, mock.callWithPrimitives(1, 2))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithSelf(mock) }
    }

    @Test
    fun testVarargsWithCompositeCombining() {
        every {
            mock.callWithVararg(
                any(),
                or(eq("1"), eq("2")),
                not(eq("2"))
            )
        } returns 1.0
        assertEquals(1.0, mock.callWithVararg(0, "1", "3"))
        assertEquals(1.0, mock.callWithVararg(1, "2", "4"))
        assertFailsWith<MokkeryRuntimeException> { mock.callWithVararg(1, "3", "4") }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithVararg(1, "1", "2") }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithVararg(1) }
    }

    @Test
    fun testDetectsLiteralsInCombinedMatchers() {
        assertFailsWith<MokkeryRuntimeException> { every { mock.callWithPrimitives(or(eq(1), 2)) } }
        assertFailsWith<MokkeryRuntimeException> { every { mock.callWithPrimitives(or(1, eq(2))) } }
        assertFailsWith<MokkeryRuntimeException> { every { mock.callWithPrimitives(or(1, 2)) } }
    }

    @Test
    fun testDetectsLiteralsInCombinedMatchersWithVarargs() {
        assertFailsWith<MokkeryRuntimeException> {
            every { mock.callWithVararg(any(), any(), and("1", eq("2"))) }
        }
        assertFailsWith<MokkeryRuntimeException> {
            every { mock.callWithVararg(any(), and("1", eq("2"))) }
        }
        assertFailsWith<MokkeryRuntimeException> {
            every { mock.callWithVararg(any(), any(), not("0")) }
        }
    }

    @Test
    fun testMixedVarargs() {
        every { mock.callWithVararg(any(), "1", *anyVarargs(), eq("3")) } returns 1.0
        assertEquals(1.0, mock.callWithVararg(1, "1", "2", "2", "3"))
        assertEquals(1.0, mock.callWithVararg(1, "1", "2", "3"))
        assertEquals(1.0, mock.callWithVararg(1, "1", "3"))
        assertFailsWith<MokkeryRuntimeException> {
            mock.callWithVararg(1, "1")
            mock.callWithVararg(1, "3")
        }
    }

    @Test
    fun testVarargsWildcards() {
        every { mock.callWithVararg(any(), "1", *varargsAny { it == "1" }, eq("3")) } returns 1.0
        every { mock.callWithVararg(any(), "1", *varargsAll { it == "0" }, eq("3")) } returns 2.0
        assertEquals(2.0, mock.callWithVararg(1, "1", "0", "0", "3"))
        assertEquals(1.0, mock.callWithVararg(1, "1", "0", "1", "3"))
        assertFailsWith<MokkeryRuntimeException> {
            assertEquals(1.0, mock.callWithVararg(1, "1", "2", "3"))
        }
    }

    @Test
    fun testDetectsAmbiguousVarargs() {
        assertFailsWith<MokkeryRuntimeException> {
            every { mock.callWithVararg(1, args = arrayOf("1", "2", *anyVarargs())) }
        }
        assertFailsWith<MokkeryRuntimeException> {
            every { mock.callWithVararg(1, args = arrayOf(*anyVarargs(), "1", "2")) }
        }
        assertFailsWith<MokkeryRuntimeException> {
            every { mock.callWithVararg(1, args = arrayOf("1", any(), "3")) }
        }
    }

    @Test
    fun testAllowsSolvingVarargsAmbiguity() {
        every { mock.callWithVararg(1, args = arrayOf(eq("1"), any(), eq("3"))) } returns 1.0
        every { mock.callWithVararg(1, args = arrayOf(eq("1"), eq("2"), *anyVarargs())) } returns 2.0
        every { mock.callWithVararg(1, args = arrayOf(*anyVarargs(), eq("1"), eq("2"))) } returns 3.0
        assertEquals(1.0, mock.callWithVararg(1, "1", "5", "3"))
        assertEquals(2.0, mock.callWithVararg(1, "1", "2", "2"))
        assertEquals(3.0, mock.callWithVararg(1, "1", "1", "2"))
    }

    @Test
    fun testVarargsWildcardsWithCapture() {
        val container = Capture.container<Array<String>>()
        every { mock.callWithVararg(any(), eq("1"), *capture(container, anyVarargs()), eq("3")) } returns 1.0
        mock.callWithVararg(1, "1", "2", "3")
        mock.callWithVararg(1, "1", "2", "3", "3")
        mock.callWithVararg(1, "1", "2", "3", "4", "3")
        val expected = listOf(arrayOf("2"), arrayOf("2", "3"), arrayOf("2", "3", "4"))
        expected.zip(container.values).forEach { (a, b) ->
            assertContentEquals(a, b)
        }
    }

    @Test
    fun testVarargsWithCapture() {
        val container = Capture.container<String>()
        every { mock.callWithVararg(any(), any(), capture(container)) } returns 1.0
        mock.callWithVararg(1, "1", "1")
        mock.callWithVararg(1, "1", "2")
        mock.callWithVararg(1, "1", "3")
        assertEquals(listOf("1", "2", "3"), container.values)
    }

    @Test
    fun testVarargsWildcardsWithCompositeMatchers() {
        every { mock.callWithVararg(any(), eq("1"), *anyVarargs(), eq("3")) } returns 1.0
        every { mock.callWithVararg(any(), eq("1"), *not(varargsAny { it == "1" }), eq("3")) } returns 2.0
        assertEquals(1.0, mock.callWithVararg(0, "1", "1", "2", "3"))
        assertEquals(1.0, mock.callWithVararg(0, "1", "1", "1", "3"))
        assertEquals(2.0, mock.callWithVararg(0, "1", "2", "2", "3"))
    }

    @Test
    fun testMixingVarargsCompositesWithLiterals() {
        every { mock.callWithVararg(any(), "1", *anyVarargs(), "3") } returns 1.0
        every { mock.callWithVararg(any(), "1", *not(varargsAny { it == "1" }), "3") } returns 2.0
        every { mock.callWithVararg(any(), neq("1"), *anyVarargs(), "3") } returns 3.0
        assertEquals(1.0, mock.callWithVararg(0, "1", "1", "2", "3"))
        assertEquals(2.0, mock.callWithVararg(0, "1", "2", "2", "3"))
        assertEquals(3.0, mock.callWithVararg(0, "2", "2", "3"))
    }

    @Test
    fun testContentDeepEqForArray() {
        every { mock.callWithArray(contentDeepEq(arrayOf(arrayOf("1"), arrayOf("2")))) } returns arrayOf("1", "2")
        assertContentEquals(arrayOf("1", "2"), mock.callWithArray(arrayOf(arrayOf("1"), arrayOf("2"))))
        assertFailsWith<MokkeryRuntimeException> {
            mock.callWithArray(arrayOf())
        }
    }

    @Test
    fun testContentEqForArray() {
        every { mock.callWithArray(contentEq(arrayOf("1", "2"))) } returns "1"
        assertEquals("1", mock.callWithArray(arrayOf("1", "2")))
        assertFailsWith<MokkeryRuntimeException> {
            mock.callWithArray(arrayOf())
        }
    }

    @Test
    fun testContentEqForIntArray() {
        every { mock.callWithIntArray(contentEq(intArrayOf(1, 2, 3))) } returns "1"
        assertEquals("1", mock.callWithIntArray(intArrayOf(1, 2, 3)))
        assertFailsWith<MokkeryRuntimeException> {
            mock.callWithIntArray(intArrayOf())
        }
    }

    @Test
    fun testIsIn() {
        every { mock.callGeneric(isIn(1, 2, 3)) } returns 1
        every { mock.callGeneric(isIn(listOf(5, 6))) } returns 2
        assertFailsWith<MokkeryRuntimeException> { mock.callGeneric(0) }
        assertEquals(1, mock.callGeneric(1))
        assertEquals(1, mock.callGeneric(2))
        assertEquals(1, mock.callGeneric(3))
        assertFailsWith<MokkeryRuntimeException> { mock.callGeneric(4) }
        assertEquals(2, mock.callGeneric(5))
        assertEquals(2, mock.callGeneric(6))
        assertFailsWith<MokkeryRuntimeException> { mock.callGeneric(7) }
    }

    @Test
    fun testIsNotIn() {
        every { mock.callGeneric(isNotIn(1, 2)) } returns 1
        every { mock.callGeneric(isNotIn(listOf(2, 3))) } returns 2
        assertEquals(2, mock.callGeneric(0))
        assertEquals(2, mock.callGeneric(1))
        assertFailsWith<MokkeryRuntimeException> { mock.callGeneric(2) }
        assertEquals(1, mock.callGeneric(3))
        assertEquals(2, mock.callGeneric(4))
    }

    @Test
    fun testCapture() {
        val slot = Capture.slot<Int>()
        every { mock.callWithPrimitives(capture(slot), 1) } returns 0.0
        mock.callWithPrimitives(2, 1)
        mock.callWithPrimitives(3, 1)
        assertEquals(3, slot.getIfPresent())
    }

    @Test
    fun testCaptureWithNonDefaultMatcher() {
        val slot = Capture.slot<Int>()
        every { mock.callWithPrimitives(capture(slot, eq(2)), 1) } returns 0.0
        mock.callWithPrimitives(2, 1)
        assertFailsWith<MokkeryRuntimeException> {
            mock.callWithPrimitives(3, 1)
        }
        assertEquals(2, slot.getIfPresent())
    }

    @Test
    fun testCaptureWithNotFullMatch() {
        val slot1 = Capture.slot<Int>()
        val slot2 = Capture.slot<Int>()
        every { mock.callWithPrimitives(capture(slot1), 1) } returns 0.0
        every { mock.callWithPrimitives(capture(slot2), 2) } returns 0.0
        mock.callWithPrimitives(2, 1)
        assertEquals(2, slot1.getIfPresent())
        assertEquals(null, slot2.getIfPresent())
    }

    @Test
    fun testCaptureCallbackIsCalled() {
        val called = mutableListOf<Int>()
        val callback = Capture.callback(callback = called::add)
        every { mock.callWithPrimitives(any()) } returns 0.0
        every { mock.callWithPrimitives(capture(callback, gte(1))) } returns 0.0
        mock.callWithPrimitives(0)
        mock.callWithPrimitives(1)
        mock.callWithPrimitives(3)
        assertEquals(listOf(1, 3), called)
    }

    @Test
    fun testOnArgCallbackIsCalled() {
        val called = mutableListOf<Int>()
        every { mock.callWithPrimitives(any()) } returns 0.0
        every { mock.callWithPrimitives(onArg(gte(1), called::add)) } returns 0.0
        mock.callWithPrimitives(0)
        mock.callWithPrimitives(1)
        mock.callWithPrimitives(3)
        assertEquals(listOf(1, 3), called)
    }
}

