package dev.mokkery.internal.interceptor

import dev.mokkery.interceptor.nextIntercept
import dev.mokkery.test.TestMokkeryCallInterceptor
import dev.mokkery.test.TestMockInterceptor
import dev.mokkery.test.runTest
import dev.mokkery.test.testBlockingCallScope
import dev.mokkery.test.testSuspendCallScope
import kotlin.test.Test
import kotlin.test.assertEquals

class HookCallInterceptorTest {

    private val hookCallInterceptor = HookCallInterceptor()
    private val nextInterceptor = TestMockInterceptor(
        interceptBlock = { -10 },
        interceptSuspendBlock = { -12 }
    )

    @Test
    fun testCallsNextInterceptWhenNoInterceptorsRegistered() {
        assertEquals(-10, hookCallInterceptor.intercept(testBlockingCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testCallsNextInterceptSuspendWhenNoInterceptorsRegistered() = runTest {
        assertEquals(-12, hookCallInterceptor.intercept(testSuspendCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testCallsRegisteredInterceptorInterceptWhenPresent() {
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptBlock = { 6 }))
        assertEquals(6, hookCallInterceptor.intercept(testBlockingCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testtCallsRegisteredInterceptorInterceptSuspendWhenPresent() = runTest {
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptSuspendBlock = { 7 }))
        assertEquals(7, hookCallInterceptor.intercept(testSuspendCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testRegisteredInterceptorIsAbleToCallNextInterceptWhenMultipleRegisteredInterceptors() {
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 1 }))
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 1 }))
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptBlock = { 1 }))
        assertEquals(3, hookCallInterceptor.intercept(testBlockingCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testRegisteredInterceptorIsAbleToCallNextInterceptSuspendWhenMultipleRegisteredInterceptors() = runTest {
        hookCallInterceptor.register(
            TestMokkeryCallInterceptor(interceptSuspendBlock = { it.nextIntercept() as Int + 2 })
        )
        hookCallInterceptor.register(
            TestMokkeryCallInterceptor(interceptSuspendBlock = { it.nextIntercept() as Int + 2 })
        )
        hookCallInterceptor.register(
            TestMokkeryCallInterceptor(interceptSuspendBlock = { 2 })
        )
        assertEquals(6, hookCallInterceptor.intercept(testSuspendCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testLastRegisteredInterceptorCallsInterceptAfterHookWhenAnyInterceptorRegistered() {
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 2}))
        hookCallInterceptor.register(TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 2}))
        assertEquals(-6, hookCallInterceptor.intercept(testBlockingCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testLastRegisteredInterceptorCallsInterceptSuspendAfterHookWhenAnyInterceptorRegistered() = runTest {
        hookCallInterceptor.register(
            TestMokkeryCallInterceptor(interceptSuspendBlock = { it.nextIntercept() as Int + 2 })
        )
        hookCallInterceptor.register(
            TestMokkeryCallInterceptor(interceptSuspendBlock = { it.nextIntercept() as Int + 2 })
        )
        assertEquals(-8, hookCallInterceptor.intercept(testSuspendCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testUnregistersInterceptorsInReversProperly() {
        val a =  TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 2 })
        val b = TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 3 })
        hookCallInterceptor.register(a)
        hookCallInterceptor.register(b)
        assertEquals(-5, hookCallInterceptor.intercept(testBlockingCallScope<Int>(context = nextInterceptor)))
        hookCallInterceptor.unregister(b)
        assertEquals(-8, hookCallInterceptor.intercept(testBlockingCallScope<Int>(context = nextInterceptor)))
        hookCallInterceptor.unregister(a)
        assertEquals(-10, hookCallInterceptor.intercept(testBlockingCallScope<Int>(context = nextInterceptor)))
    }

    @Test
    fun testUnregistersInterceptorsInRegistrationOrderProperly() {
        val a = TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 2 })
        val b = TestMokkeryCallInterceptor(interceptBlock = { it.nextIntercept() as Int + 3 })
        hookCallInterceptor.register(a)
        hookCallInterceptor.register(b)
        assertEquals(-5, hookCallInterceptor.intercept(testBlockingCallScope<Int>(context = nextInterceptor)))
        hookCallInterceptor.unregister(a)
        assertEquals(-7, hookCallInterceptor.intercept(testBlockingCallScope<Int>(context = nextInterceptor)))
        hookCallInterceptor.unregister(b)
        assertEquals(-10, hookCallInterceptor.intercept(testBlockingCallScope<Int>(context = nextInterceptor)))
    }
}
