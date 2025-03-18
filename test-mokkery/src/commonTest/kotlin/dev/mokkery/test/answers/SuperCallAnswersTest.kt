package dev.mokkery.test.answers

import dev.mokkery.answering.SuperCall.Companion.original
import dev.mokkery.answering.SuperCall.Companion.originalWith
import dev.mokkery.answering.SuperCall.Companion.superOf
import dev.mokkery.answering.SuperCall.Companion.superWith
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.DefaultsInterfaceLevel1
import dev.mokkery.test.DefaultsInterfaceLevel2
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SuperCallAnswersTest {

    private val mock = mock<DefaultsInterfaceLevel1<Int>>()


    @Test
    fun testCallsOriginalFromScope() {
        every { mock.call(any(), any()) } calls {
            callOriginal()
        }
        assertEquals(ComplexType("1"), mock.call(1, ComplexType))
    }

    @Test
    fun testCallsOriginalSuspendFromScope() = runTest {
        everySuspend { mock.callSuspend(any(), any()) } calls {
            callOriginal()
        }
        assertEquals(ComplexType("2"), mock.callSuspend(2, ComplexType))
    }

    @Test
    fun testCallsOriginalWithArgsFromScope() {
        every { mock.call(any(), any()) } calls {
            callOriginalWith(4, ComplexType)
        }
        assertEquals(ComplexType("4"), mock.call(1))
    }

    @Test
    fun testCallsOriginalWithArgsSuspendFromScope() = runTest {
        everySuspend { mock.callSuspend(any()) } calls {
            callOriginalWith(5, ComplexType)
        }
        assertEquals(ComplexType("5"), mock.callSuspend(1))
    }

    @Test
    fun testCallsSuperFromScope() {
        every { mock.call(any(), any()) } calls {
            callSuper(DefaultsInterfaceLevel2::class)
        }
        assertEquals(ComplexType("2"), mock.call(1))
    }

    @Test
    fun testCallsSuperSuspendFromScope() = runTest {
        everySuspend { mock.callSuspend(any(), any()) } calls {
            callSuper(DefaultsInterfaceLevel2::class)
        }
        assertEquals(ComplexType("2"), mock.callSuspend(1))
    }


    @Test
    fun testCallsSuperWithArgsFromScope() {
        every { mock.call(any(), any()) } calls {
            callSuperWith(DefaultsInterfaceLevel2::class, 5, ComplexType)
        }
        assertEquals(ComplexType("6"), mock.call(1))
    }

    @Test
    fun testCallsSuperWithArgsSuspendFromScope() = runTest {
        everySuspend { mock.callSuspend(any(), any()) } calls {
            callSuperWith(DefaultsInterfaceLevel2::class, 5, ComplexType)
        }
        assertEquals(ComplexType("6"), mock.callSuspend(1))
    }

    @Test
    fun testCallsOriginal() {
        every { mock.call(any(), any()) } calls original
        assertEquals(ComplexType("1"), mock.call(1, ComplexType))
    }

    @Test
    fun testCallsOriginalWithoutDirectOverride() {
        every { mock.callIndirectDefault(any(), any()) } calls original
        assertEquals(ComplexType("2"), mock.callIndirectDefault(1))
    }

    @Test
    fun testCallsSuspendOriginalWithoutDirectOverride() = runTest {
        everySuspend { mock.callSuspendIndirectDefault(any(), any()) } calls original
        assertEquals(ComplexType("2"), mock.callSuspendIndirectDefault(1))
    }

    @Test
    fun testCallsOriginalSuspend() = runTest {
        everySuspend { mock.callSuspend(any(), any()) } calls original
        assertEquals(ComplexType("2"), mock.callSuspend(2, ComplexType))
    }

    @Test
    fun testCallsOriginalWithArgs() {
        every { mock.call(any(), any()) } calls originalWith(4, ComplexType)
        assertEquals(ComplexType("4"), mock.call(1))
    }

    @Test
    fun testCallsOriginalWithArgsSuspend() = runTest {
        everySuspend { mock.callSuspend(any()) } calls originalWith(5, ComplexType)
        assertEquals(ComplexType("5"), mock.callSuspend(1))
    }


    @Test
    fun testCallsSuper() {
        every { mock.call(any(), any()) } calls superOf<DefaultsInterfaceLevel2<Int>>()
        assertEquals(ComplexType("2"), mock.call(1))
    }

    @Test
    fun testCallsSuperSuspend() = runTest {
        everySuspend { mock.callSuspend(any(), any()) } calls superOf<DefaultsInterfaceLevel2<Int>>()
        assertEquals(ComplexType("2"), mock.callSuspend(1))
    }

    @Test
    fun testCallsSuperWithArgs() {
        every { mock.call(any(), any()) } calls superWith<DefaultsInterfaceLevel2<Int>>(5, ComplexType)
        assertEquals(ComplexType("6"), mock.call(1))
    }

    @Test
    fun testCallsSuperWithArgsSuspend() = runTest {
        everySuspend { mock.callSuspend(any(), any()) } calls superWith<DefaultsInterfaceLevel2<Int>>(5, ComplexType)
        assertEquals(ComplexType("6"), mock.callSuspend(1))
    }
}
