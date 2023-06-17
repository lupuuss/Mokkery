package dev.mokkery.internal.tracing

import dev.mokkery.internal.MokkeryToken
import dev.mokkery.internal.MokkeryInterceptor
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlin.reflect.KClass

internal interface CallTracingInterceptor : MokkeryInterceptor {

    val unverified: List<CallTrace>

    val all: List<CallTrace>

    fun reset()

    fun markVerified(trace: CallTrace)
}

internal fun CallTracingInterceptor(receiver: String, clock: CallTraceClock): CallTracingInterceptor {
    return CallTracingInterceptorImpl(receiver, clock)
}

private class CallTracingInterceptorImpl(
    private val receiver: String,
    private val clock: CallTraceClock,
) : CallTracingInterceptor {

    private var verified by atomic(listOf<CallTrace>())
    private var _all by atomic(listOf<CallTrace>())

    private val lock = reentrantLock()

    override val unverified: List<CallTrace> get() = lock.withLock { _all - verified }
    override val all: List<CallTrace> = _all

    override fun reset() = lock.withLock {
        verified = emptyList()
        _all = emptyList()
    }

    override fun markVerified(trace: CallTrace) {
        lock.withLock { verified += trace }
    }

    override fun interceptCall(signature: String, returnType: KClass<*>, varArgPosition: Int, vararg args: Any?): Any? {
        lock.withLock {
            _all += CallTrace(receiver, signature, args.toList(), clock.nextStamp())
        }
        return MokkeryToken.CALL_NEXT
    }

}
