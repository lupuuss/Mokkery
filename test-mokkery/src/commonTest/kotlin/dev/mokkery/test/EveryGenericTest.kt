package dev.mokkery.test

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class EveryGenericTest {

    private val genericDependency = mock<TestGenericInterface<String>>()

    @Test
    fun testMocksProperty() {
        every { genericDependency.value } returns "1"
        assertEquals("1", genericDependency.value)
    }

    @Test
    fun testMocksMethod() {
        every { genericDependency.call("1") } returns true
        assertEquals(true, genericDependency.call("1"))
    }

    @Test
    fun testMocksGenericMethodWithComplexType() {
        every { genericDependency.callGeneric(any<List<String>>()) } returns listOf("1")
        assertEquals(listOf("1"), genericDependency.callGeneric(listOf()))
    }

    @Test
    fun testMocksGenericMethodWithUnit() {
        every { genericDependency.callGeneric(Unit) } returns Unit
        assertEquals(Unit, genericDependency.callGeneric(Unit))
    }

    @Test
    fun testMocksGenericMethodWithPrimitive() {
        every { genericDependency.callGeneric(1) } returns 1
        assertEquals(1, genericDependency.callGeneric(1))
    }

    @Test
    fun testMocksGenericMethodWithNullablePrimitive() {
        every { genericDependency.callGeneric<Int?>(1) } returns 1
        assertEquals(1, genericDependency.callGeneric(1))
    }

    @Test
    fun testMocksBoundedGenericMethod() {
        every { genericDependency.callBoundedGeneric(1) } returns 2
        assertEquals(2, genericDependency.callBoundedGeneric(1))
    }

    @Test
    fun testMocksSuspendBoundedGenericMethod() = runTest {
        everySuspend { genericDependency.callSuspendBoundedGeneric(1) } returns 2
        assertEquals(2, genericDependency.callSuspendBoundedGeneric(1))
    }

    @Test
    fun testMocksExtensionFunction() {
        every { genericDependency.run { any<List<String>>().extension() } } returns "123"
        assertEquals("123", genericDependency.run { listOf<String>().extension() })
    }

    @Test
    fun testMocksGenericExtensionFunction() {
        every { genericDependency.run { any<List<Comparable<*>>>().genericExtension() } } returns "123"
        assertEquals("123", genericDependency.run { listOf<Int>().genericExtension() })
    }

    @Test
    fun testMocksExtensionProperty() {
        every { genericDependency.run { any<List<String>>().listSize } } returns 1
        genericDependency.run { assertEquals(1, listOf<String>().listSize) }
    }

    @Test
    fun testMocksGenericExtensionProperty() {
        every { genericDependency.run { any<List<Any?>>().genericListSize } } returns 1
        genericDependency.run { assertEquals(1, listOf<Int>().genericListSize) }
    }
}
