package dev.mokkery.internal.tracing

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.context.toCallTrace
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.instanceId
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import kotlin.collections.component1
import kotlin.collections.component2

internal interface CallTracingRegistry : MokkeryContext.Element {

    override val key: MokkeryContext.Key<*> get() = Key

    val all: List<CallTrace>

    fun trace(scope: MokkeryCallScope)

    fun acquireSession(): Session

    companion object Key : MokkeryContext.Key<CallTracingRegistry>

    interface Session : AutoCloseable {
        val unverified: List<CallTrace>

        fun resetAll()

        fun markVerified(trace: CallTrace)
    }

    interface CompositeSession : Session {
        val sessions: Map<MokkeryInstanceId, Session>
    }
}

internal val MokkeryScope.callTracing: CallTracingRegistry
    get() = mokkeryContext.require(CallTracingRegistry)

internal fun CallTracingRegistry(): CallTracingRegistry = CallTracingRegistryImpl()

internal inline fun <R> CallTracingRegistry.withSession(
    block: CallTracingRegistry.Session.() -> R
): R = acquireSession().use(block)

internal fun MokkeryCollection.acquireSession(): CallTracingRegistry.CompositeSession = CompositeSessionImpl(this)

internal inline fun <R> MokkeryCollection.withTracingSession(
    block: CallTracingRegistry.CompositeSession.() -> R
): R = acquireSession().use(block)

private class CallTracingRegistryImpl : CallTracingRegistry {

    private val allTraces = mutableListOf<CallTrace>()
    private val allTracesLock = reentrantLock()

    private val verifiedTraces = linkedSetOf<CallTrace>()
    private val verifiedTracesLock = reentrantLock()

    override val all get() = allTracesLock.withLock { allTraces.toMutableList() }

    override fun trace(scope: MokkeryCallScope) = allTracesLock.withLock {
        allTraces += scope.toCallTrace(scope.tools.callsCounter.next())
    }

    override fun acquireSession() = object : CallTracingRegistry.Session {

        private val allSnapshot = allTracesLock.withLock { allTraces.toMutableList() }

        init {
            verifiedTracesLock.lock()
        }

        override val unverified: List<CallTrace>
            get() = allSnapshot - verifiedTraces

        override fun markVerified(trace: CallTrace) {
            verifiedTraces.add(trace)
        }

        override fun resetAll() {
            verifiedTraces.clear()
            allTracesLock.withLock { allTraces.removeAll(allSnapshot.toSet()) }
            allSnapshot.clear()
        }

        override fun close() = verifiedTracesLock.unlock()
    }

    override fun toString(): String = "CallTracingRegistry(all=$all)"
}

private class CompositeSessionImpl(
    collection: MokkeryCollection,
) : CallTracingRegistry.CompositeSession {

    override val sessions = collection
        .scopes
        .sortedBy { it.instanceId }
        .associateTo(linkedMapOf()) { it.instanceId to it.callTracing.acquireSession() }

    override val unverified: List<CallTrace>
        get() = sessions
            .values
            .flatMap(CallTracingRegistry.Session::unverified)
            .sorted()

    override fun resetAll() {
        sessions
            .values
            .forEach { it.resetAll() }
    }

    override fun markVerified(trace: CallTrace) = sessions
        .getValue(trace.instanceId)
        .markVerified(trace)

    override fun close() {
        var error: MokkeryRuntimeException? = null
        sessions.forEach { (_, lock) ->
            try {
                lock.close()
            } catch (e: Throwable) {
                if (error == null) error = MokkeryRuntimeException("Failure while releasing locks!")
                error.addSuppressed(e)
            }
        }
        error?.let { throw it }
    }
}
