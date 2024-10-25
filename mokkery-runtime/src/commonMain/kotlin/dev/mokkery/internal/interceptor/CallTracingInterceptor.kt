package dev.mokkery.internal.interceptor

import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.interceptor.nextIntercept
import dev.mokkery.interceptor.nextInterceptSuspend
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.context.toTrace
import dev.mokkery.internal.context.tools
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

internal interface CallTracingInterceptor : MokkeryCallInterceptor {

    val unverified: List<CallTrace>

    val all: List<CallTrace>

    fun reset()

    fun markVerified(trace: CallTrace)
}

internal fun CallTracingInterceptor(): CallTracingInterceptor = CallTracingInterceptorImpl()

private class CallTracingInterceptorImpl() : CallTracingInterceptor {

    private val verified = mutableListOf<CallTrace>()
    private val _all = mutableListOf<CallTrace>()

    private val lock = reentrantLock()

    override val unverified: List<CallTrace> get() = lock.withLock { _all - verified.toSet() }
    override val all: List<CallTrace> get() = lock.withLock { _all.toMutableList() }

    override fun reset() = lock.withLock {
        verified.clear()
        _all.clear()
    }

    override fun markVerified(trace: CallTrace) {
        lock.withLock { verified += trace }
    }

    override fun intercept(scope: MokkeryCallScope): Any? {
        val context = scope.context
        val counter = context.tools.callsCounter
        lock.withLock { _all += context.toTrace(counter.next()) }
        return scope.nextIntercept()
    }

    override suspend fun interceptSuspend(scope: MokkeryCallScope): Any? {
        val context = scope.context
        val counter = context.tools.callsCounter
        lock.withLock { _all += context.toTrace(counter.next()) }
        return scope.nextInterceptSuspend()
    }
}
