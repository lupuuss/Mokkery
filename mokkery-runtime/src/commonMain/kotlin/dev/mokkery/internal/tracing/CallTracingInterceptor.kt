package dev.mokkery.internal.tracing

import dev.mokkery.internal.CallContext
import dev.mokkery.internal.Counter
import dev.mokkery.internal.MokkeryInterceptor
import dev.mokkery.internal.MokkeryToken
import dev.mokkery.internal.id
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

internal interface CallTracingInterceptor : MokkeryInterceptor {

    val unverified: List<CallTrace>

    val all: List<CallTrace>

    fun reset()

    fun markVerified(trace: CallTrace)
}

internal fun CallTracingInterceptor(
    counter: Counter = Counter.calls
): CallTracingInterceptor = CallTracingInterceptorImpl(counter)

private class CallTracingInterceptorImpl(
    private val counter: Counter,
) : CallTracingInterceptor {

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

    override fun interceptCall(context: CallContext): Any {
        lock.withLock { _all += context.toTrace() }
        return MokkeryToken.CallNext
    }

    private fun CallContext.toTrace() = CallTrace(instance.id, name, args, counter.next())

}
