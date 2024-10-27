package dev.mokkery.internal.interceptor

import dev.mokkery.interceptor.nextIntercept
import dev.mokkery.interceptor.nextInterceptSuspend
import dev.mokkery.test.TestMokkeryCallInterceptor
import dev.mokkery.test.TestNextCallInterceptor
import dev.mokkery.test.runTest
import dev.mokkery.test.testCallScope
import kotlin.test.Test
import kotlin.test.assertEquals

class HookCallInterceptorTest {

    private val hookCallInterceptor = HookCallInterceptor()
    private val nextInterceptor = TestNextCallInterceptor(
        interceptBlock = { -10 },
        interceptSuspendBlock = { -12 }
    )

    @Test
    fun testCallsNextInterceptWhenNoInterceptorsRegistered() {
        assertEquals(-10, hookCallInterceptor.intercept(testCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testCallsNextInterceptSuspendWhenNoInterceptorsRegistered() = runTest {
        assertEquals(-12, hookCallInterceptor.interceptSuspend(testCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testCallsRegisteredInterceptorInterceptWhenPresent() {
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptBlock = { 6 }))
        assertEquals(6, hookCallInterceptor.intercept(testCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testtCallsRegisteredInterceptorInterceptSuspendWhenPresent() = runTest {
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptSuspendBlock = { 7 }))
        assertEquals(7, hookCallInterceptor.interceptSuspend(testCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testRegisteredInterceptorIsAbleToCallNextInterceptWhenMultipleRegisteredInterceptors() {
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 1 }))
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 1 }))
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptBlock = { 1 }))
        assertEquals(3, hookCallInterceptor.intercept(testCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testRegisteredInterceptorIsAbleToCallNextInterceptSuspendWhenMultipleRegisteredInterceptors() = runTest {
        hookCallInterceptor.register(
            TestMokkeryCallInterceptor(interceptSuspendBlock = { it.nextInterceptSuspend() as Int + 2 })
        )
        hookCallInterceptor.register(
            TestMokkeryCallInterceptor(interceptSuspendBlock = { it.nextInterceptSuspend() as Int + 2 })
        )
        hookCallInterceptor.register(
            TestMokkeryCallInterceptor(interceptSuspendBlock = { 2 })
        )
        assertEquals(6, hookCallInterceptor.interceptSuspend(testCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testLastRegisteredInterceptorCallsInterceptAfterHookWhenAnyInterceptorRegistered() {
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 2}))
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 2}))
        assertEquals(-6, hookCallInterceptor.intercept(testCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testLastRegisteredInterceptorCallsInterceptSuspendAfterHookWhenAnyInterceptorRegistered() = runTest {
        hookCallInterceptor.register(
            TestMokkeryCallInterceptor(interceptSuspendBlock = { it.nextInterceptSuspend() as Int + 2 })
        )
        hookCallInterceptor.register(
            TestMokkeryCallInterceptor(interceptSuspendBlock = { it.nextInterceptSuspend() as Int + 2 })
        )
        assertEquals(-8, hookCallInterceptor.interceptSuspend(testCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testUnregistersInterceptorsInReversProperly() {
        val a =  TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 2 })
        val b = TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 3 })
        hookCallInterceptor.register(a)
        hookCallInterceptor.register(b)
        assertEquals(-5, hookCallInterceptor.intercept(testCallScope<Int>(context = nextInterceptor)))
        hookCallInterceptor.unregister(b)
        assertEquals(-8, hookCallInterceptor.intercept(testCallScope<Int>(context = nextInterceptor)))
        hookCallInterceptor.unregister(a)
        assertEquals(-10, hookCallInterceptor.intercept(testCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testUnregistersInterceptorsInRegistrationOrderProperly() {
        val a = TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 2 })
        val b = TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 3 })
        hookCallInterceptor.register(a)
        hookCallInterceptor.register(b)
        assertEquals(-5, hookCallInterceptor.intercept(testCallScope<Int>(context = nextInterceptor)))
        hookCallInterceptor.unregister(a)
        assertEquals(-7, hookCallInterceptor.intercept(testCallScope<Int>(context = nextInterceptor)))
        hookCallInterceptor.unregister(b)
        assertEquals(-10, hookCallInterceptor.intercept(testCallScope<Int>(context = nextInterceptor)))
    }
}
