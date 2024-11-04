package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.RegularMethodsInterface
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MocksRegularMethodsTest {

    private val mock = mock<RegularMethodsInterface>()

    @Test
    fun testUnit() {
        every { mock.callUnit(Unit) } returns Unit
        assertEquals(Unit, mock.callUnit(Unit))
        verify { mock.callUnit(Unit) }
    }

    @Test
    fun testNothing() {
        every { mock.callNothing(1) } throws IllegalStateException()
        assertFailsWith<IllegalStateException> { mock.callNothing(1) }
        verify { mock.callNothing(1) }
    }

    @Test
    fun testPrimitive() {
        every { mock.callPrimitive(any<Int>()) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        verify { mock.callPrimitive(1) }
    }

    @Test
    fun testComplex() {
        every { mock.callComplex(any()) } returns ComplexType.Companion
        assertEquals(ComplexType.Companion, mock.callComplex(ComplexType.Companion))
        verify { mock.callComplex(ComplexType.Companion) }
    }

    @Test
    fun testIntArray() {
        every { mock.callIntArray(any()) } returns intArrayOf(1, 2, 3)
        assertContentEquals(intArrayOf(1, 2, 3), mock.callIntArray(intArrayOf()))
        verify { mock.callIntArray(any()) }
    }

    @Test
    fun testArray() {
        every { mock.callArray(any()) } returns arrayOf(ComplexType.Companion)
        assertContentEquals(arrayOf(ComplexType.Companion), mock.callArray(arrayOf()))
        verify { mock.callArray(any()) }
    }

    @Test
    fun testOverloads() {
        every { mock.callOverloaded(any<Int>()) } returns 1
        every { mock.callOverloaded(any<Double>()) } returns 1.0
        every { mock.callOverloaded(any<ComplexType>()) } returns ComplexType.Companion
        assertEquals(1, mock.callOverloaded(1))
        assertEquals(1.0, mock.callOverloaded(1.0))
        assertEquals(ComplexType.Companion, mock.callOverloaded(ComplexType.Companion))
        verify {
            mock.callOverloaded(1)
            mock.callOverloaded(1.0)
            mock.callOverloaded(ComplexType.Companion)
        }
    }

    @Test
    fun testPrimitiveWithDefaults() {
        every { mock.callPrimitiveWithDefaults() } returns 1
        assertEquals(1, mock.callPrimitiveWithDefaults())
        verify { mock.callPrimitiveWithDefaults() }
    }

    @Test
    fun testComplexWithDefaults() {
        every { mock.callComplexWithDefaults() } returns ComplexType.Companion
        assertEquals(ComplexType.Companion, mock.callComplexWithDefaults())
        verify { mock.callComplexWithDefaults() }
    }

    @Test
    fun testPrimitiveExtension() {
        every { mock.run { 1.callPrimitiveExtension() } } returns 1
        assertEquals(1, mock.run { 1.callPrimitiveExtension() })
        verify { mock.run { 1.callPrimitiveExtension() } }
    }

    @Test
    fun testComplexExtension() {
        every { mock.run { ComplexType.callComplexExtension() } } returns ComplexType.Companion
        assertEquals(ComplexType.Companion, mock.run { ComplexType.callComplexExtension() })
        verify { mock.run { ComplexType.callComplexExtension() } }
    }
}
