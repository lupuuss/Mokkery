package dev.mokkery.internal.interceptor

import dev.mokkery.interceptor.MokkeryBlockingCallScope
import dev.mokkery.interceptor.MokkeryCallInterceptor
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.interceptor.MokkerySuspendCallScope
import dev.mokkery.interceptor.nextIntercept
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.context.toCallTrace
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

    override fun intercept(scope: MokkeryBlockingCallScope): Any? {
        traceCallOf(scope)
        return scope.nextIntercept()
    }

    override suspend fun intercept(scope: MokkerySuspendCallScope): Any? {
        traceCallOf(scope)
        return scope.nextIntercept()
    }
    private fun traceCallOf(scope: MokkeryCallScope) {
        val counter = scope.mokkeryContext.tools.callsCounter
        lock.withLock { _all += scope.toCallTrace(counter.next()) }
    }
}
