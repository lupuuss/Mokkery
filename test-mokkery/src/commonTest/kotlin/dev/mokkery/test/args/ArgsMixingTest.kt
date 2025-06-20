package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returns
import dev.mokkery.debug.printMokkeryDebug
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.eq
import dev.mokkery.matcher.gte
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
    fun testExtractMatchersToVariables() {
        every {
            val a = or(1, gte(2))
            mock.callPrimitive(input = a)
        } returns 2
        assertEquals(2, mock.callPrimitive(1))
        assertEquals(2, mock.callPrimitive(2))
        assertEquals(2, mock.callPrimitive(3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(0) }
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
            mock.callManyPrimitives(a = not(and(neq(1), neq(2))), b = and(neq(4.0), neq(5.0)))
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
    fun testLiteralsCombinedWithCompositeMatchers() {
        every { mock.callPrimitive(or(2, 4, gte(6))) } returns 2
        assertEquals(2, mock.callPrimitive(2))
        assertEquals(2, mock.callPrimitive(4))
        assertEquals(2, mock.callPrimitive(6))
        assertEquals(2, mock.callPrimitive(7))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(3) }
    }

    @Test
    fun testLiteralsWithCombinedMatchersAndVarargs() {
        every { mock.callPrimitiveVarargs(1, 2, or(10, 11), 3) } returns 4
        assertEquals(4, mock.callPrimitiveVarargs(1, 2, 10, 3))
        assertEquals(4, mock.callPrimitiveVarargs(1, 2, 11, 3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 2, 12, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(0, 2, 10, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 3, 10, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(1, 2, 10, 4) }
    }

    @Test
    fun testMixedVarargs() {
        every { mock.callPrimitiveVarargs(any(), 1, *anyVarargsInt(), 3) } returns 1
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
    fun testNamedVarargArray() {
        every { mock.callPrimitiveVarargs(1, inputs = intArrayOf(1, any(), 3)) } returns 1
        every { mock.callPrimitiveVarargs(1, inputs = intArrayOf(1, 2, *anyVarargsInt())) } returns 2
        every { mock.callPrimitiveVarargs(1, inputs = intArrayOf(*anyVarargsInt(), 1, 2)) } returns 3
        assertEquals(1, mock.callPrimitiveVarargs(1, 1, 5, 3))
        assertEquals(2, mock.callPrimitiveVarargs(1, 1, 2, 2))
        assertEquals(3, mock.callPrimitiveVarargs(1, 1, 1, 2))
    }

    @Test
    fun testSpreadLiteralsWithMatchers() {
        val values = intArrayOf(2)
        every { mock.callPrimitiveVarargs(0, 1, *values, any()) } returns 1
        assertEquals(1, mock.callPrimitiveVarargs(0, 1, 2, 1))
        assertEquals(1, mock.callPrimitiveVarargs(0, 1, 2, 5))
        assertEquals(1, mock.callPrimitiveVarargs(0, 1, 2, 10))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(5, 1, 2, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(0, 5, 2, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(0, 1, 5, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(0, 1, 2) }
    }

    @Test
    fun testSpreadLiterals() {
        val values = intArrayOf(2)
        every { mock.callPrimitiveVarargs(0, 1, *values, 3) } returns 1
        assertEquals(1, mock.callPrimitiveVarargs(0, 1, 2, 3))
        assertEquals(1, mock.callPrimitiveVarargs(0, 1, 2, 3))
        assertEquals(1, mock.callPrimitiveVarargs(0, 1, 2, 3))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(5, 1, 2, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(0, 5, 2, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(0, 1, 5, 3) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(0, 1, 2, 5) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(0, 1, 2) }
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
        every { mock.callPrimitiveVarargs(any(), 1, *capture(container, anyVarargsInt()), 3) } returns 1
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
        every { mock.callPrimitiveVarargs(any(), 1, *anyVarargsInt(), 3) } returns 1
        every { mock.callPrimitiveVarargs(any(), 1, *not(varargsIntAny { it == 1 }), 3) } returns 2
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
    fun testOnlyVarargMatcher() {
        every { mock.callPrimitiveVarargs(inputs = anyVarargsInt()) } returns 1
        assertEquals(1, mock.callPrimitiveVarargs(inputs = intArrayOf(1, 2)))
        assertEquals(1, mock.callPrimitiveVarargs(inputs = intArrayOf(1)))
        assertEquals(1, mock.callPrimitiveVarargs())
    }

    @Test
    fun testNoVarargs() {
        every { mock.callPrimitiveVarargs() } returns 1
        assertEquals(1, mock.callPrimitiveVarargs())
        assertEquals(1, mock.callPrimitiveVarargs(1))
        assertEquals(1, mock.callPrimitiveVarargs(inputs = intArrayOf()))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(2) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(inputs = intArrayOf(1)) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveVarargs(inputs = intArrayOf(1, 2)) }
    }
}
