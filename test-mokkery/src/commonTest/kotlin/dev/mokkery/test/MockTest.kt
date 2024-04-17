package dev.mokkery.test

import dev.mokkery.MockMode.autoUnit
import dev.mokkery.MockMode.autofill
import dev.mokkery.MockMode.strict
import dev.mokkery.MockMode.original
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class MockTest {

    @Test
    fun testReturnsMockableInterfaceImpl() {
        val mocked = mock<TestInterface> {
            every { callWithPrimitives(1) } returns 1.0
        }
        assertEquals(1.0, mocked.callWithPrimitives(1))
    }

    @Test
    fun testReturnsMockableGenericInterfaceImpl() {
        val mocked = mock<TestGenericInterface<Int>> {
            every { call(1) } returns true
        }
       assertEquals(true, mocked.call(1))
    }

    @Test
    fun testReturnsMockableClassImpl() {
        val mocked = mock<TestClass> {
            every { call() } returns "1"
        }
        assertEquals("1", mocked.call())
        assertEquals("Ignored inline property", mocked.inlineProperty)
        assertEquals("Ignored inline method", mocked.inlineMethod())
        assertEquals("Ignored final method", mocked.finalMethod())
    }

    @Test
    fun testReturnsMockableGenericClassImpl() {
        val mocked = mock<TestGenericClass<String>> {
            every { call("123") } returns "321"
        }
        assertEquals("321", mocked.call("123"))
    }

    @Test
    fun testReturnsMockableKotlinCollection() {
        val mocked = mock<List<String>> {
            every { get(0) } returns "foo"
        }
        assertEquals("foo", mocked.get(0))
    }


    @Test
    fun testAutofillAlwaysReturnsSuccessfullyOnNonSuspendingMethods() {
        val mock = mock<TestInterface>(autofill)
        mock.callWithPrimitives(1)
        mock.callWithVararg(1, "1", "2")
        mock.run { 1.callWithExtensionReceiver() }
    }

    @Test
    fun testAutoUnitReturnsSuccessfullyFromUnitMethods() {
        val mock = mock<TestInterface>(autoUnit)
        mock.callUnit()
    }

    @Test
    fun testAutoUnitFailsOnNonUnitMethods() {
        val mock = mock<TestInterface>(autoUnit)
        assertFails { mock.callWithPrimitives(1) }
    }

    @Test
    fun testAutoUnitFailsOnNonUnitMethodsSuspend() = runTest {
        val mock = mock<TestInterface>(autoUnit)
        assertFails { mock.callWithSuspension(1) }
    }

    @Test
    fun testStrictAlwaysFails() {
        val mock = mock<TestInterface>(strict)
        assertFails { mock.callWithPrimitives(1) }
        assertFails { mock.callUnit() }
    }

    @Test
    fun testStrictAlwaysFailsSuspend() = runTest {
        val mock = mock<TestInterface>(strict)
        assertFails { mock.callWithSuspension(1) }
    }

    @Test
    fun testOriginalFailsOnNonDefaultMethod() {
        val mock = mock<TestInterface>(original)
        assertFails { mock.callWithString("") }
        assertFails { mock.callUnit() }
    }

    @Test
    fun testOriginalFailsOnNonDefaultMethodSuspend() = runTest {
        val mock = mock<TestInterface>(original)
        assertFails { mock.callWithSuspension(1) }
    }

    @Test
    fun testOriginalCallsOriginalImpl() = runTest {
        val mock = mock<TestInterface>(original)
        assertEquals(3, mock.callWithDefault(1))
    }


    @Test
    fun testOriginalCallsOriginalImplSuspend() = runTest {
        val mock = mock<TestInterface>(original)
        assertEquals(3, mock.fetchWithDefault(1))
    }

    @Test
    fun testMocksWithAllOpenPlugin() {
        val mock = mock<FinalClass> {
            every { finalMethod() } returns "123"
        }
        assertEquals("123", mock.finalMethod())
    }

    @Test
    fun testMocksFunctionalTypeWithPrimitives() {
        val mock = mock<(Int, Int) -> Double> {
            every { invoke(1, 2) } returns 1.0
        }
        assertEquals(1.0, mock(1, 2))
    }

    @Test
    fun testMocksFunctionalTypeWithComplexTypes() {
        val mock = mock<(List<Int>) -> List<String>> {
            every { invoke(listOf(1)) } returns listOf("1")
        }
        assertEquals(listOf("1"), mock(listOf(1)))
    }

    @Test
    fun testMocksFunctionalTypeWithReceiver() {
        val mock = mock<Int.(Int) -> Double> {
            every { invoke(1, 2) } returns 1.0
        }
        assertEquals(1.0, mock(1, 2))
    }

    @Test
    fun testMocksSuspendFunctionalTypeWithPrimitives() = runTest {
        val mock = mock<suspend (Int) -> Double> {
            everySuspend { invoke(1) } returns 1.0
        }
        assertEquals(1.0, mock(1))
    }

    @Test
    fun testMocksSuspendFunctionalTypeWithComplexTypes() = runTest {
        val mock = mock<suspend (List<Int>) -> List<String>> {
            everySuspend { invoke(listOf(1)) } returns listOf("1")
        }
        assertEquals(listOf("1"), mock(listOf(1)))
    }
}

