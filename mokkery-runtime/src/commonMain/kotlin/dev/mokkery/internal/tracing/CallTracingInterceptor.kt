package dev.mokkery.internal.tracing

import dev.mokkery.internal.CallContext
import dev.mokkery.internal.MokkeryToken
import dev.mokkery.internal.MokkeryInterceptor
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

internal interface CallTracingInterceptor : MokkeryInterceptor {

    val unverified: List<CallTrace>

    val all: List<CallTrace>

    fun reset()

    fun markVerified(trace: CallTrace)
}

internal fun CallTracingInterceptor(clock: CallTraceClock): CallTracingInterceptor = CallTracingInterceptorImpl(clock)

private class CallTracingInterceptorImpl(
    private val clock: CallTraceClock,
) : CallTracingInterceptor {

    private var verified by atomic(listOf<CallTrace>())
    private var _all by atomic(listOf<CallTrace>())

    private val lock = reentrantLock()

    override val unverified: List<CallTrace> get() = lock.withLock { _all - verified.toSet() }
    override val all: List<CallTrace> = _all

    override fun reset() = lock.withLock {
        verified = emptyList()
        _all = emptyList()
    }

    override fun markVerified(trace: CallTrace) {
        lock.withLock { verified += trace }
    }

    override fun interceptCall(context: CallContext): Any {
        lock.withLock { _all += context.toTrace() }
        return MokkeryToken.CALL_NEXT
    }

    private fun CallContext.toTrace() = CallTrace(receiver, signature, args, clock.nextStamp())

}
