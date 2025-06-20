package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.MocksCollection
import dev.mokkery.internal.MutableMocksCollection
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

internal interface MocksRegistry : MokkeryContext.Element {

    override val key get() = Key

    val mocks: MocksCollection

    fun register(mock: MokkeryInstanceScope)

    companion object Key : MokkeryContext.Key<MocksRegistry>
}

internal fun MocksRegistry(mocks: List<MokkeryInstanceScope> = emptyList()): MocksRegistry {
    return MocksRegistryImpl(mocks)
}

private class MocksRegistryImpl(mocks: List<MokkeryInstanceScope>) : MocksRegistry {

    private val _mocks = MutableMocksCollection(mocks)
    private val lock = ReentrantLock()

    override val mocks: MocksCollection get() = lock.withLock { _mocks }

    override fun register(mock: MokkeryInstanceScope) = lock.withLock { _mocks.upsertScope(mock) }

    override fun toString(): String = "MocksRegistry { ${mocks.scopes.joinToString()} }"
}
