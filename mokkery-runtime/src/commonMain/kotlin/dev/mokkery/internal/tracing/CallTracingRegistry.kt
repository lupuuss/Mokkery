package dev.mokkery.internal.tracing

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.MokkeryCallScope
import dev.mokkery.internal.context.toCallTrace
import dev.mokkery.internal.context.tools
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

internal interface CallTracingRegistry : MokkeryContext.Element {

    override val key: MokkeryContext.Key<*> get() = Key

    val unverified: List<CallTrace>

    val all: List<CallTrace>

    fun reset()

    fun markVerified(trace: CallTrace)

    fun trace(scope: MokkeryCallScope)

    companion object Key : MokkeryContext.Key<CallTracingRegistry>
}

internal val MokkeryScope.callTracing: CallTracingRegistry
    get() = mokkeryContext.require(CallTracingRegistry)

internal fun CallTracingRegistry(): CallTracingRegistry = CallTracingRegistryImpl()

private class CallTracingRegistryImpl : CallTracingRegistry {

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

    override fun trace(scope: MokkeryCallScope) {
        val counter = scope.tools.callsCounter
        lock.withLock { _all += scope.toCallTrace(counter.next()) }
    }

    override fun toString(): String = "CallTracingRegistry@${hashCode()}"
}
