package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.GenericFunctionsInterface
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksGenericFunctionsTest {

    private val mock = mock<GenericFunctionsInterface<Int?>>()

    @Test
    fun testCallGeneric() {
        every { mock.callGeneric<String>(any()) } returns "Hello!"
        assertEquals("Hello!", mock.callGeneric(1))
        verify { mock.callGeneric<String>(any()) }
    }

    @Test
    fun testCallGenericNullabilityMarkers() {
        every { mock.callGenericNullabilityMarkers<String>(any()) } returns "Hello!"
        assertEquals("Hello!", mock.callGenericNullabilityMarkers<String>(1))
        verify { mock.callGenericNullabilityMarkers<String>(any()) }
    }

    @Test
    fun testCallGenericNestedNullabilityMarkers() {
        every { mock.callGenericNestedNullabilityMarkers<String>(any()) } returns listOf("Hello!")
        assertEquals(listOf("Hello!"), mock.callGenericNestedNullabilityMarkers<String>(listOf(1)))
        verify { mock.callGenericNestedNullabilityMarkers<String>(any()) }
    }

    @Test
    fun testCallGenericBound() {
        every { mock.callGenericBound<String>(any()) } returns "Hello!"
        assertEquals("Hello!", mock.callGenericBound<String>(1))
        verify { mock.callGenericBound<String>(any()) }
    }

    @Test
    fun testCallGenericBoundRecursiveParam() {
        every { mock.callGenericBoundRecursiveParam<String>(any()) } returns "Hello!"
        assertEquals("Hello!", mock.callGenericBoundRecursiveParam<String>(1))
        verify { mock.callGenericBoundRecursiveParam<String>(any()) }
    }

    @Test
    fun testCallGenericBoundParentParam() {
        every { mock.callGenericBoundParentParam<Int>(any()) } returns 12
        assertEquals(12, mock.callGenericBoundParentParam<Int>(1))
        verify { mock.callGenericBoundParentParam<Int>(any()) }
    }

    @Test
    fun testCallGenericBoundNestedParentParam() {
        every { mock.callGenericBoundNestedParentParam<MutableList<Int>>(any()) } returns mutableListOf(1)
        assertEquals(mutableListOf(1), mock.callGenericBoundNestedParentParam<MutableList<Int>>(1))
        verify { mock.callGenericBoundNestedParentParam<MutableList<Int>>(any()) }
    }

    @Test
    fun testCallGenericMultipleBounds() {
        every { mock.callGenericMultipleBounds<String>(any()) } returns "Hello!"
        assertEquals("Hello!", mock.callGenericMultipleBounds<String>(1))
        verify { mock.callGenericMultipleBounds<String>(any()) }
    }

    @Test
    fun testCallGenericWithStarProjection() {
        every { mock.callGenericWithStarProjection(any()) } returns listOf<Int>(1)
        assertEquals(listOf<Int>(1), mock.callGenericWithStarProjection(listOf<String>()))
        verify { mock.callGenericWithStarProjection(any()) }
    }

    @Test
    fun testCallSelf() {
        val subMock = mock<SubGenericFunctionsInterface>()
        every { mock.callSelf<SubGenericFunctionsInterface>(any()) } returns mock
        assertEquals(mock, mock.callSelf(subMock))
        verify { mock.callSelf<SubGenericFunctionsInterface>(any()) }
    }
}

private interface SubGenericFunctionsInterface : GenericFunctionsInterface<SubGenericFunctionsInterface>
