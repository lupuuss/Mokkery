package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.templating.ext
import dev.mokkery.spy
import dev.mokkery.test.ComplexType
import dev.mokkery.test.SpyTestInterface
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.Result.Companion.success
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpyTest {

    private val obj = SpyTestInterface.Companion<Int?>()
    private val spy = spy<SpyTestInterface<Int?>>(obj)

    @Test
    fun testPropertyDelegatesToSpiedObject() {
        spy.property = 1
        assertEquals(1, obj.property)
        obj.property = 2
        assertEquals(2, spy.property)
        verify {
            spy.property = 1
            spy.property
        }
    }

    @Test
    fun testCallDelegatesToSpiedObject() {
        assertEquals(ComplexType.Companion("2"), spy.call(ComplexType.Companion("1")))
        verify { spy.call(ComplexType.Companion("1")) }
    }

    @Test
    fun testExtPropertyDelegatesToSpiedObject() {
        spy.run {
            assertEquals("1", 1.extProperty)
            assertFailsWith<IllegalArgumentException> { 1.extProperty = "2" }
                .message
                .let { assertEquals("1 - 2", it) }
        }
        verify {
            spy.ext {
                1.extProperty
                1.extProperty = "2"
            }
        }
    }

    @Test
    fun testCallExtensionDelegatesToSpiedObject() {
        val i = 10
        spy.run { assertEquals(i.hashCode(), i.callExtension()) }
        verify {
            spy.run { i.callExtension() }
        }
    }

    @Test
    fun testCallSuspendDelegatesToSpiedObject() = runTest {
        assertEquals(33, spy.callSuspend(33))
        verifySuspend { spy.callSuspend(33) }
    }

    @Test
    fun testCallResultDelegatesToSpiedObject() = runTest {
        assertEquals(success(2), spy.callResult(success<Int?>(2)))
        verifySuspend { spy.callResult(success(2)) }
    }

    @Test
    fun testCallNothingDelegatesToSpiedObject() = runTest {
        assertFailsWith<IllegalArgumentException> { spy.callNothing() }
        verify { spy.callNothing() }
    }

    @Test
    fun testAllowsChangingPropertyBehavior() {
        every { spy.property = any<Int>() } throws ArithmeticException()
        every { spy.property } returns 10
        assertFailsWith<ArithmeticException> { spy.property = 1 }
        spy.property
        verify {
            spy.property = 1
            spy.property
        }
    }

    @Test
    fun testAllowsChangingCallBehavior() {
        every { spy.call(any()) } returns ComplexType.Companion
        assertEquals(ComplexType.Companion, spy.call(ComplexType.Companion("1")))
    }

    @Test
    fun testAllowsChangingExtPropertyBehavior() {
        every { spy.ext { any<Int>().extProperty } } returns "Hello!"
        every { spy.ext { any<Int>().extProperty = any<String>() } } throws ArithmeticException()
        assertEquals("Hello!", spy.run { 1.extProperty })
        assertFailsWith<ArithmeticException> { spy.run { 1.extProperty = "" } }
    }

    @Test
    fun testAllowsChangingCallExtensionBehavior() {
        every { spy.ext { any<Int>().callExtension() } } returns 3
        assertEquals(3, spy.run { 1.callExtension() })
    }

    @Test
    fun testAllowsChangingCallSuspendBehavior() = runTest {
        everySuspend { spy.callSuspend<Int>(any()) } returns 3
        assertEquals(3, spy.callSuspend<Int>(33))
    }

    @Test
    fun testAllowsChangingCallResultBehavior() = runTest {
        everySuspend { spy.callResult(any()) } returns success(1)
        assertEquals(success(1), spy.callResult(success<Int?>(2)))
    }

    @Test
    fun testAllowsChanginCallNothingBehavior() = runTest {
        every { spy.callNothing() } throws ArithmeticException()
        assertFailsWith<ArithmeticException> { spy.callNothing() }
    }
}
