package dev.mokkery.test

import dev.mokkery.MockMode.autoUnit
import dev.mokkery.MockMode.autofill
import dev.mokkery.MockMode.strict
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class MockTest {

    @Test
    fun testReturnsMockableInterfaceImpl() {
        val mocked = mock<TestDependency> {
            every { callWithPrimitives(1) } returns 1.0
        }
        assertEquals(1.0, mocked.callWithPrimitives(1))
    }

    @Test
    fun testReturnsMockableClassImpl() {
        val mocked = mock<TestClass> {
            every { call() } returns "1"
        }
        assertEquals("1", mocked.call())
    }

    @Test
    fun testAutofillAlwaysReturnsSuccessfullyOnNonSuspendingMethods() {
        val mock = mock<TestDependency>(autofill)
        mock.callWithPrimitives(1)
        mock.callWithVararg(1, "1", "2")
        mock.run { 1.callWithExtensionReceiver() }
    }

    @Test
    fun testAutoUnitReturnsSuccessfullyFromUnitMethods() = runTest {
        val mock = mock<TestDependency>(autoUnit)
        mock.callUnit()
    }

    @Test
    fun testAutoUnitFailsOnNonUnitMethods() = runTest {
        val mock = mock<TestDependency>(autoUnit)
        assertFails { mock.callWithPrimitives(1) }
        assertFails { mock.callWithSuspension(1) }
    }

    @Test
    fun testStrictAlwaysFails() = runTest {
        val mock = mock<TestDependency>(strict)
        assertFails { mock.callWithPrimitives(1) }
        assertFails { mock.callWithSuspension(1) }
        assertFails { mock.callUnit() }
    }
}

