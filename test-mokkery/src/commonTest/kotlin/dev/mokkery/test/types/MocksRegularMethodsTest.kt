package dev.mokkery.test.types

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.templating.ext
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.RegularMethodsInterface
import dev.mokkery.test.assertVerified
import dev.mokkery.verify
import dev.mokkery.verifySuspend
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
        assertEquals(1, mock.callPrimitiveWithDefaults(ComplexType, 1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveWithDefaults(primitiveInput = 2) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveWithDefaults(complexInput = ComplexType("1")) }
        verify { mock.callPrimitiveWithDefaults() }
    }

    @Test
    fun testComplexWithDefaults() {
        every { mock.callComplexWithDefaults() } returns ComplexType.Companion
        assertEquals(ComplexType.Companion, mock.callComplexWithDefaults())
        assertEquals(ComplexType.Companion, mock.callComplexWithDefaults(ComplexType, 1))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveWithDefaults(primitiveInput = 2) }
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitiveWithDefaults(complexInput = ComplexType("1")) }
        verify { mock.callComplexWithDefaults() }
    }

    @Test
    fun testDependantDefaults() {
        every { mock.callWithDependantDefaults(any()) } returnsArgAt 1
        every { mock.callWithDependantDefaults(any(), "custom") } returns ""
        assertEquals("ComplexTypeImpl(id=1)", mock.callWithDependantDefaults(ComplexType("1")))
        assertEquals("ComplexTypeImpl(id=2)", mock.callWithDependantDefaults(ComplexType("2")))
        assertEquals("", mock.callWithDependantDefaults(ComplexType("3"), "custom"))
        assertFailsWith<MokkeryRuntimeException> {
            mock.callWithDependantDefaults(ComplexType, name = "customName")
        }
        verify {
            mock.callWithDependantDefaults(ComplexType("1"))
            mock.callWithDependantDefaults(ComplexType("2"))
        }
        assertVerified {
            verifySuspend {
                mock.callWithDependantDefaults(ComplexType("3"))
            }
        }
    }

    @Test
    fun testPrimitiveExtension() {
        every { mock.ext { 1.callPrimitiveExtension() } } returns 1
        assertEquals(1, mock.run { 1.callPrimitiveExtension() })
        verify { mock.ext { 1.callPrimitiveExtension() } }
    }

    @Test
    fun testComplexExtension() {
        every { mock.ext { ComplexType.callComplexExtension() } } returns ComplexType.Companion
        assertEquals(ComplexType.Companion, mock.run { ComplexType.callComplexExtension() })
        verify { mock.ext { ComplexType.callComplexExtension() } }
    }
}
