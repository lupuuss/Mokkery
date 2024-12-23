package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.SuspendMethodsInterface
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MocksSuspendMethodsTest {

    private val mock = mock<SuspendMethodsInterface>()

    @Test
    fun testUnit() = runTest {
        everySuspend { mock.callUnit(Unit) } returns Unit
        assertEquals(Unit, mock.callUnit(Unit))
        verifySuspend { mock.callUnit(Unit) }
    }

    @Test
    fun testNothing() = runTest {
        everySuspend { mock.callNothing(1) } throws IllegalStateException()
        assertFailsWith<IllegalStateException> { mock.callNothing(1) }
        verifySuspend { mock.callNothing(1) }
    }

    @Test
    fun testPrimitive() = runTest {
        everySuspend { mock.callPrimitive(any<Int>()) } returns 1
        assertEquals(1, mock.callPrimitive(1))
        verifySuspend { mock.callPrimitive(1) }
    }

    @Test
    fun testComplex() = runTest {
        everySuspend { mock.callComplex(any()) } returns ComplexType.Companion
        assertEquals(ComplexType.Companion, mock.callComplex(ComplexType.Companion))
        verifySuspend { mock.callComplex(ComplexType.Companion) }
    }

    @Test
    fun testIntArray() = runTest {
        everySuspend { mock.callIntArray(any()) } returns intArrayOf(1, 2, 3)
        assertContentEquals(intArrayOf(1, 2, 3), mock.callIntArray(intArrayOf()))
        verifySuspend { mock.callIntArray(any()) }
    }

    @Test
    fun testArray() = runTest {
        everySuspend { mock.callArray(any()) } returns arrayOf(ComplexType.Companion)
        assertContentEquals(arrayOf(ComplexType.Companion), mock.callArray(arrayOf()))
        verifySuspend { mock.callArray(any()) }
    }

    @Test
    fun testOverloads() = runTest {
        everySuspend { mock.callOverloaded(any<Int>()) } returns 1
        everySuspend { mock.callOverloaded(any<Double>()) } returns 1.0
        everySuspend { mock.callOverloaded(any<String>()) } returns "Test"
        everySuspend { mock.callOverloaded(any<ComplexType>()) } returns ComplexType.Companion
        assertEquals(1, mock.callOverloaded(1))
        assertEquals(1.0, mock.callOverloaded(1.0))
        assertEquals("Test", mock.callOverloaded("Test"))
        assertEquals(ComplexType.Companion, mock.callOverloaded(ComplexType.Companion))
        verifySuspend {
            mock.callOverloaded(1)
            mock.callOverloaded(1.0)
            mock.callOverloaded("Test")
            mock.callOverloaded(ComplexType.Companion)
        }
    }

    @Test
    fun testPrimitiveWithDefaults() = runTest {
        everySuspend { mock.callPrimitiveWithDefaults() } returns 1
        assertEquals(1, mock.callPrimitiveWithDefaults())
        verifySuspend { mock.callPrimitiveWithDefaults() }
    }

    @Test
    fun testComplexWithDefaults() = runTest {
        everySuspend { mock.callComplexWithDefaults() } returns ComplexType.Companion
        assertEquals(ComplexType.Companion, mock.callComplexWithDefaults())
        verifySuspend { mock.callComplexWithDefaults() }
    }

    @Test
    fun testPropagatesClassCastExceptionWhenOccursInArgument() = runTest {
        // ClassCastException is ignored when suspend method is called with default parameters, but it should
        // be thrown when it occurs before registering the template
        assertFailsWith<ClassCastException> {
            everySuspend { mock.callComplexWithDefaults(complexInput = 1 as ComplexType) } returns ComplexType
        }
    }

    @Test
    fun testPrimitiveExtension() = runTest {
        everySuspend { mock.run { 1.callPrimitiveExtension() } } returns 1
        assertEquals(1, mock.run { 1.callPrimitiveExtension() })
        verifySuspend { mock.run { 1.callPrimitiveExtension() } }
    }

    @Test
    fun testComplexExtension() = runTest {
        everySuspend { mock.run { ComplexType.callComplexExtension() } } returns ComplexType.Companion
        assertEquals(ComplexType.Companion, mock.run { ComplexType.callComplexExtension() })
        verifySuspend { mock.run { ComplexType.callComplexExtension() } }
    }
}

