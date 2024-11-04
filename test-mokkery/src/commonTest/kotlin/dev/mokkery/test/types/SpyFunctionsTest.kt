package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.spy
import dev.mokkery.test.ComplexType
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SpyFunctionsTest {

    private val func: (Int) -> ComplexType = { ComplexType(it.toString()) }
    private val funcSuspend: suspend (Int) -> ComplexType = { ComplexType((it + 2).toString()) }
    private val funcSpy = spy(func)
    private val funcSuspendSpy = spy(funcSuspend)

    @Test
    fun testDelegatesToSpiedFunction() {
        assertEquals(ComplexType("3"), funcSpy(3))
        verify { funcSpy(3) }
    }

    @Test
    fun testDelegatesToSpiedSuspendFunction() = runTest {
        assertEquals(ComplexType("5"), funcSuspendSpy(3))
        verifySuspend { funcSuspendSpy(3) }
    }

    @Test
    fun testAllowsChangingSpiedFunctionBehavior() {
        every { funcSpy(any()) } returns ComplexType
        assertEquals(ComplexType, funcSpy(3))
    }

    @Test
    fun testAllowsChangingSpiedSuspendFunctionBehavior() = runTest {
        everySuspend { funcSuspendSpy(any()) } returns ComplexType
        assertEquals(ComplexType, funcSuspendSpy(3))
    }
}
