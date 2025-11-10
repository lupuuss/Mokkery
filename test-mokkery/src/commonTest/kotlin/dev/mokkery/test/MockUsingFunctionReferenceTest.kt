package dev.mokkery.test

import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MockUsingFunctionReferenceTest {

    @Test
    fun testEvery() {
        val mock = mock<RegularMethodsInterface> {
            every(::callPrimitive) returnsArgAt 0
        }
        every(mock::callComplex) returns ComplexType
        assertEquals(3, mock.callPrimitive(3))
        assertEquals(ComplexType, mock.callComplex(ComplexType))
    }

    @Test
    fun testEverySuspend() = runTest {
        val mock = mock<SuspendMethodsInterface> { everySuspend(::callPrimitive) returnsArgAt 0 }
        everySuspend(mock::callComplex) returns ComplexType
        assertEquals(3, mock.callPrimitive(3))
        assertEquals(ComplexType, mock.callComplex(ComplexType))
    }

    @Test
    fun testEveryGeneric() {
        val mock = mock<GenericFunctionsInterface<Int>> { every(::call) returnsArgAt 0 }
        every<Int>(mock::callGeneric) returns 2
        assertEquals(1, mock.call(1))
        assertEquals(2, mock.callGeneric(2))
    }

    @Test
    fun testEverySuspendGeneric() = runTest {
        val mock = mock<GenericSuspendFunctionsInterface<Int>> {
            everySuspend(::call) returnsArgAt 0
        }
        everySuspend<Int>(mock::callGeneric) returns 2
        assertEquals(1, mock.call(1))
        assertEquals(2, mock.callGeneric(2))
    }

}


