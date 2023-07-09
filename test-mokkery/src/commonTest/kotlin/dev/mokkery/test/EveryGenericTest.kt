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
    fun testMocksGenericMethod() {
        everySuspend { genericDependency.callGeneric(1) } returns 2
        assertEquals(2, genericDependency.callGeneric(1))
    }

    @Test
    fun testMocksSuspendGenericMethod() = runTest {
        everySuspend { genericDependency.callSuspendGeneric(1) } returns 2
        assertEquals(2, genericDependency.callSuspendGeneric(1))
    }

    @Test
    fun testMocksExtensionFunction() {
        every { genericDependency.run { any<List<String>>().extension() } } returns "123"
        assertEquals("123", genericDependency.run { listOf<String>().extension() })
    }

    @Test
    fun testMocksGenericExtensionFunction() {
        every { genericDependency.run { any<List<Any?>>().genericExtension() } } returns "123"
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
