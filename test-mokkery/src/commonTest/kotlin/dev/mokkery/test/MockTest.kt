package dev.mokkery.test

import dev.mokkery.MockMode.autoUnit
import dev.mokkery.MockMode.autofill
import dev.mokkery.MockMode.strict
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFails

class MockTest {

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

