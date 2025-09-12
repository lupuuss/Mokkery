package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MutableMokkeryCollection
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

internal interface MokkeryInstancesRegistry : MokkeryContext.Element {

    override val key get() = Key

    val collection: MokkeryCollection

    fun register(instance: MokkeryInstanceScope)

    companion object Key : MokkeryContext.Key<MokkeryInstancesRegistry>
}

internal fun MokkeryInstancesRegistry(instances: List<MokkeryInstanceScope> = emptyList()): MokkeryInstancesRegistry {
    return MokkeryInstancesRegistryImpl(instances)
}

private class MokkeryInstancesRegistryImpl(mocks: List<MokkeryInstanceScope>) : MokkeryInstancesRegistry {

    private val _mocks = MutableMokkeryCollection(mocks)
    private val lock = ReentrantLock()

    override val collection: MokkeryCollection get() = lock.withLock { _mocks }

    override fun register(instance: MokkeryInstanceScope) = lock.withLock { _mocks.upsertScope(instance) }

    override fun toString(): String = "MocksRegistry { ${collection.scopes.joinToString()} }"
}
