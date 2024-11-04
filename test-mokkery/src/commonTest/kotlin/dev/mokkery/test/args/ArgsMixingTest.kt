package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.eq
import dev.mokkery.matcher.logical.and
import dev.mokkery.matcher.logical.not
import dev.mokkery.matcher.logical.or
import dev.mokkery.matcher.lt
import dev.mokkery.matcher.neq
import dev.mokkery.matcher.varargs.anyVarargsInt
import dev.mokkery.matcher.varargs.varargsIntAll
import dev.mokkery.matcher.varargs.varargsIntAny
import dev.mokkery.mock
import dev.mokkery.test.ComplexArgsInterface
import dev.mokkery.test.ComplexType
import dev.mokkery.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ArgsMixingTest {

    private val mock = mock<ComplexArgsInterface>()

    @Test
    fun testSupportsNamedParametersWithMixedLiteralsAndMatchers() {
        every { mock.callDefaults(c = "2", b = lt(1.0), a = 1) } returns ComplexType
        assertEquals(ComplexType, mock.callDefaults(1, -1.0, "2"))
    }

    @Test
    fun testChangingArgsOrder() {
        every { mock.callDefaults(c = "4", b = 3.0, a = 2) } returns ComplexType.Companion
        mock.callDefaults(2, 3.0, "4")
        verify { mock.callDefaults(b = 3.0, c = "4", a = 2) }
    }

    @Test
    fun testCompositeMatchersCombining() {
        every {
            mock.callManyPrimitives(
                a = not(and(neq(1), neq(2))),
                b = and(neq(4.0), neq(5.0))
            )
        } returns ComplexType.Companion
        assertEquals(ComplexType.Companion, mock.callManyPrimitives(1, 3.0))
        assertEquals(ComplexType.Companion, mock.callManyPrimitives(2, 3.0))
        assertEquals(ComplexType.Companion, mock.callManyPrimitives(1, 1.0))
        assertEquals(ComplexType.Companion, mock.callManyPrimitives(1, 2.0))
        assertFailsWith<MokkeryRuntimeException> { mock.callManyPrimitives(4, 2.0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callManyPrimitives(1, 4.0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callManyPrimitives(1, 5.0) }
    }

    @Test
    fun testVarargsWithCompositeCombining() {
        every {
            mock.callPrimitiveVarargs(
                any(),
                or(eq(1), eq(2)),
                not(eq(2))
            )
        } returns 1
        assertEquals(1, mock.callPrimitiveVarargs(0, 1, 3))
        assertEquals(1, mock.callPrimitiveVarargs(1, 2, 4))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 3, 4) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 1, 2) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1) }
    }

    @Test
    fun testDetectsLiteralsInCombinedMatchers() {
        assertFailsWith<MokkeryRuntimeException> { every { mock.callDefaults(or(eq(1), 2)) } }
        assertFailsWith<MokkeryRuntimeException> { every { mock.callDefaults(or(1, eq(2))) } }
        assertFailsWith<MokkeryRuntimeException> { every { mock.callDefaults(or(1, 2)) } }
    }

    @Test
    fun testDetectsLiteralsInCombinedMatchersWithVarargs() {
        assertFailsWith<MokkeryRuntimeException> {
            every { mock.callPrimitiveVarargs(any(), any(), and(1, eq(2))) }
        }
        assertFailsWith<MokkeryRuntimeException> {
            every { mock.callPrimitiveVarargs(any(), and(1, eq(2))) }
        }
        assertFailsWith<MokkeryRuntimeException> {
            every { mock.callPrimitiveVarargs(any(), any(), not(0)) }
        }
    }

    @Test
    fun testMixedVarargs() {
        every { mock.callPrimitiveVarargs(any(), 1, *anyVarargsInt(), eq(3)) } returns 1
        assertEquals(1, mock.callPrimitiveVarargs(1, 1, 2, 2, 3))
        assertEquals(1, mock.callPrimitiveVarargs(1, 1, 2, 3))
        assertEquals(1, mock.callPrimitiveVarargs(1, 1, 3))
        assertFailsWith<MokkeryRuntimeException> {
            mock.callPrimitiveVarargs(1, 1)
            mock.callPrimitiveVarargs(1, 3)
        }
    }

    @Test
    fun testVarargsWildcards() {
        every { mock.callPrimitiveVarargs(any(), 1, *varargsIntAny { it == 1 }, eq(3)) } returns 1
        every { mock.callPrimitiveVarargs(any(), 1, *varargsIntAll { it == 0 }, eq(3)) } returns 2
        assertEquals(2, mock.callPrimitiveVarargs(1, 1, 0, 0, 3))
        assertEquals(1, mock.callPrimitiveVarargs(1, 1, 0, 1, 3))
        assertFailsWith<MokkeryRuntimeException> {
            assertEquals(1, mock.callPrimitiveVarargs(1, 1, 2, 3))
        }
    }

    @Test
    fun testDetectsAmbiguousVarargs() {
        assertFailsWith<MokkeryRuntimeException> {
            every { mock.callPrimitiveVarargs(1, inputs = intArrayOf(1, 2, *anyVarargsInt())) }
        }
        assertFailsWith<MokkeryRuntimeException> {
            every { mock.callPrimitiveVarargs(1, inputs = intArrayOf(*anyVarargsInt(), 1, 2)) }
        }
        assertFailsWith<MokkeryRuntimeException> {
            every { mock.callPrimitiveVarargs(1, inputs = intArrayOf(1, any(), 3)) }
        }
    }

    @Test
    fun testAllowsSolvingVarargsAmbiguity() {
        every { mock.callPrimitiveVarargs(1, inputs = intArrayOf(eq(1), any(), eq(3))) } returns 1
        every { mock.callPrimitiveVarargs(1, inputs = intArrayOf(eq(1), eq(2), *anyVarargsInt())) } returns 2
        every { mock.callPrimitiveVarargs(1, inputs = intArrayOf(*anyVarargsInt(), eq(1), eq(2))) } returns 3
        assertEquals(1, mock.callPrimitiveVarargs(1, 1, 5, 3))
        assertEquals(2, mock.callPrimitiveVarargs(1, 1, 2, 2))
        assertEquals(3, mock.callPrimitiveVarargs(1, 1, 1, 2))
    }

    @Test
    fun testVarargsWithCapture() {
        val container = Capture.container<Int>()
        every { mock.callPrimitiveVarargs(any(), any(), capture(container)) } returns 1
        mock.callPrimitiveVarargs(1, 1, 1)
        mock.callPrimitiveVarargs(1, 1, 2)
        mock.callPrimitiveVarargs(1, 1, 3)
        assertEquals(listOf(1, 2, 3), container.values)
    }

    @Test
    fun testVarargsWildcardsWithCapture() {
        val container = Capture.container<IntArray>()
        every { mock.callPrimitiveVarargs(any(), eq(1), *capture(container, anyVarargsInt()), eq(3)) } returns 1
        mock.callPrimitiveVarargs(1, 1, 2, 3)
        mock.callPrimitiveVarargs(1, 1, 2, 3, 3)
        mock.callPrimitiveVarargs(1, 1, 2, 3, 4, 3)
        val expected = listOf(intArrayOf(2), intArrayOf(2, 3), intArrayOf(2, 3, 4))
        expected.zip(container.values).forEach { (a, b) ->
            assertContentEquals(a, b)
        }
    }

    @Test
    fun testVarargsWildcardsWithCompositeMatchers() {
        every { mock.callPrimitiveVarargs(any(), eq(1), *anyVarargsInt(), eq(3)) } returns 1
        every { mock.callPrimitiveVarargs(any(), eq(1), *not(varargsIntAny { it == 1 }), eq(3)) } returns 2
        assertEquals(1, mock.callPrimitiveVarargs(0, 1, 1, 2, 3))
        assertEquals(1, mock.callPrimitiveVarargs(0, 1, 1, 1, 3))
        assertEquals(2, mock.callPrimitiveVarargs(0, 1, 2, 2, 3))
    }

    @Test
    fun testMixingVarargsCompositesWithLiterals() {
        every { mock.callPrimitiveVarargs(any(), 1, *anyVarargsInt(), 3) } returns 1
        every { mock.callPrimitiveVarargs(any(), 1, *not(varargsIntAny { it == 1 }), 3) } returns 2
        every { mock.callPrimitiveVarargs(any(), neq(1), *anyVarargsInt(), 3) } returns 3
        assertEquals(1, mock.callPrimitiveVarargs(0, 1, 1, 2, 3))
        assertEquals(2, mock.callPrimitiveVarargs(0, 1, 2, 2, 3))
        assertEquals(3, mock.callPrimitiveVarargs(0, 2, 2, 3))
    }

    @Test
    fun testPropagatesClassCastExceptionWhenOccursInArgument() = runTest {
        assertFailsWith<ClassCastException> {
            everySuspend { mock.callPrimitive(Unit as Int) } returns 1
        }
    }
}
