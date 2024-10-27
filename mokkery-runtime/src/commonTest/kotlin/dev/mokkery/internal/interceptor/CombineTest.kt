package dev.mokkery.internal.interceptor

import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkerySuspendCallScope
import dev.mokkery.interceptor.nextIntercept
import dev.mokkery.test.TestMokkeryContext
import dev.mokkery.test.runTest
import dev.mokkery.test.testBlockingCallScope
import dev.mokkery.test.testSuspendCallScope
import kotlin.test.Test
import kotlin.test.assertEquals

class CombineTest {

    private val interceptCalls = mutableListOf<MokkeryCallInterceptor>()
    private val interceptSuspendCalls = mutableListOf<MokkeryCallInterceptor>()

    @Test
    fun testCallAllInterceptorsInTheChain() {
        val a = interceptor { it.nextIntercept() }
        val b = interceptor { it.nextIntercept() }
        val c = interceptor { 1 }
        val interceptor = combine(a, b, c)
        assertEquals(1, interceptor.intercept(MokkeryBlockingCallScope()))
        assertEquals(listOf(a, b, c), interceptCalls)
    }

    @Test
    fun testCallAllInterceptorsInTheChainSuspend() = runTest {
        val a = interceptorSuspend { it.nextIntercept() }
        val b = interceptorSuspend { it.nextIntercept() }
        val c = interceptorSuspend { 2 }
        val interceptor = combine(a, b, c)
        assertEquals(2, interceptor.intercept(MokkerySuspendCallScope()))
        assertEquals(listOf(a, b, c), interceptSuspendCalls)
    }

    @Test
    fun testAllowsCombiningInterceptorResults() {
        val a = interceptor { it.nextIntercept() }
        val b = interceptor { it.nextIntercept() as Int + 3 }
        val c = interceptor { 2 }
        val interceptor = combine(a, b, c)
        assertEquals(5, interceptor.intercept(MokkeryBlockingCallScope()))
        assertEquals(listOf(a, b, c), interceptCalls)
    }

    @Test
    fun testAllowsCombiningInterceptorResultsSuspend() = runTest {
        val a = interceptorSuspend { it.nextIntercept() }
        val b = interceptorSuspend { it.nextIntercept() as Int + 4 }
        val c = interceptorSuspend { 3 }
        val interceptor = combine(a, b, c)
        assertEquals(7, interceptor.intercept(MokkerySuspendCallScope()))
        assertEquals(listOf(a, b, c), interceptSuspendCalls)
    }

    @Test
    fun testAllowsChangingContext() {
        val a = interceptor { it.nextIntercept() }
        val b = interceptor { it.nextIntercept(TestMokkeryContext(10)) }
        val c = interceptor { it.context[TestMokkeryContext]?.value }
        val result = combine(a, b, c)
            .intercept(MokkeryBlockingCallScope(TestMokkeryContext(2)))
        assertEquals(10, result)
    }

    @Test
    fun testAllowsChangingContextSuspend() = runTest {
        val a = interceptorSuspend { it.nextIntercept() }
        val b = interceptorSuspend { it.nextIntercept(TestMokkeryContext(11)) }
        val c = interceptorSuspend { it.context[TestMokkeryContext]?.value }
        val result = combine(a, b, c)
            .intercept(MokkerySuspendCallScope(TestMokkeryContext(3)))
        assertEquals(11, result)
    }

    @Test
    fun testAllowsStoppingChain() {
        val a = interceptor { it.nextIntercept() }
        val b = interceptor { 10 }
        val c = interceptor { 11 }
        val result = combine(a, b, c).intercept(testBlockingCallScope<Unit>())
        assertEquals(10, result)
    }

    @Test
    fun testAllowsStoppingChainSuspend() = runTest {
        val a = interceptorSuspend { it.nextIntercept() }
        val b = interceptorSuspend { 11 }
        val c = interceptorSuspend { 12 }
        val result = combine(a, b, c).intercept(testSuspendCallScope<Unit>())
        assertEquals(11, result)
    }

    private fun interceptor(
        block: (MokkeryBlockingCallScope) -> Any?,
    ): MokkeryCallInterceptor {
        return object : MokkeryCallInterceptor {
            override fun intercept(scope: MokkeryBlockingCallScope): Any? {
                interceptCalls += this
                return block(scope)
            }

            override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
                error("Undefined behaviour")
            }
        }
    }

    private fun interceptorSuspend(
        block: suspend (MokkerySuspendCallScope) -> Any?,
    ): MokkeryCallInterceptor {
        return object : MokkeryCallInterceptor {

            override fun intercept(scope: MokkeryBlockingCallScope): Any? {
                error("Undefined behaviour")
            }

            override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
                interceptSuspendCalls += this
                return block(scope)
            }
        }
    }
}

