package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.GenericFunctionsInterface
import dev.mokkery.test.GenericSuspendFunctionsInterface
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksGenericSuspendFunctionsTest {

    private val mock = mock<GenericSuspendFunctionsInterface<Int?>>()

    @Test
    fun testCallGeneric() = runTest {
        everySuspend { mock.callGeneric<String>(any()) } returns "Hello!"
        assertEquals("Hello!", mock.callGeneric(1))
        verifySuspend { mock.callGeneric<String>(any()) }
    }

    @Test
    fun testCallGenericNullabilityMarkers() = runTest {
        everySuspend { mock.callGenericNullabilityMarkers<String>(any()) } returns "Hello!"
        assertEquals("Hello!", mock.callGenericNullabilityMarkers<String>(1))
        verifySuspend { mock.callGenericNullabilityMarkers<String>(any()) }
    }

    @Test
    fun testCallGenericNestedNullabilityMarkers() = runTest {
        everySuspend { mock.callGenericNestedNullabilityMarkers<String>(any()) } returns listOf("Hello!")
        assertEquals(listOf("Hello!"), mock.callGenericNestedNullabilityMarkers<String>(listOf(1)))
        verifySuspend { mock.callGenericNestedNullabilityMarkers<String>(any()) }
    }

    @Test
    fun testCallGenericBound() = runTest {
        everySuspend { mock.callGenericBound<String>(any()) } returns "Hello!"
        assertEquals("Hello!", mock.callGenericBound<String>(1))
        verifySuspend { mock.callGenericBound<String>(any()) }
    }

    @Test
    fun testCallGenericBoundRecursiveParam() = runTest {
        everySuspend { mock.callGenericBoundRecursiveParam<String>(any()) } returns "Hello!"
        assertEquals("Hello!", mock.callGenericBoundRecursiveParam<String>(1))
        verifySuspend { mock.callGenericBoundRecursiveParam<String>(any()) }
    }

    @Test
    fun testCallGenericBoundParentParam() = runTest {
        everySuspend { mock.callGenericBoundParentParam<Int>(any()) } returns 12
        assertEquals(12, mock.callGenericBoundParentParam<Int>(1))
        verifySuspend { mock.callGenericBoundParentParam<Int>(any()) }
    }

    @Test
    fun testCallGenericBoundNestedParentParam() = runTest {
        everySuspend { mock.callGenericBoundNestedParentParam<MutableList<Int>>(any()) } returns mutableListOf(1)
        assertEquals(mutableListOf(1), mock.callGenericBoundNestedParentParam<MutableList<Int>>(1))
        verifySuspend { mock.callGenericBoundNestedParentParam<MutableList<Int>>(any()) }
    }

    @Test
    fun testCallGenericMultipleBounds() = runTest {
        everySuspend { mock.callGenericMultipleBounds<String>(any()) } returns "Hello!"
        assertEquals("Hello!", mock.callGenericMultipleBounds<String>(1))
        verifySuspend { mock.callGenericMultipleBounds<String>(any()) }
    }

    @Test
    fun testCallGenericWithStarProjection() = runTest {
        everySuspend { mock.callGenericWithStarProjection(any()) } returns listOf<Int>(1)
        assertEquals(listOf<Int>(1), mock.callGenericWithStarProjection(listOf<String>()))
        verifySuspend { mock.callGenericWithStarProjection(any()) }
    }

    @Test
    fun testCallSelf() = runTest {
        val subMock = mock<SubGenericSuspendFunctionsInterface>()
        everySuspend { mock.callSelf<SubGenericSuspendFunctionsInterface>(any()) } returns mock
        assertEquals(mock, mock.callSelf(subMock))
        verifySuspend { mock.callSelf<SubGenericSuspendFunctionsInterface>(any()) }
    }
}

private interface SubGenericSuspendFunctionsInterface : GenericSuspendFunctionsInterface<SubGenericSuspendFunctionsInterface>
