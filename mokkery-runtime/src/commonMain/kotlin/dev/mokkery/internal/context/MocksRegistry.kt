package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.utils.MocksCollection
import dev.mokkery.internal.utils.MutableMocksCollection
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

private class MocksRegistryImpl(mocks: List<MokkeryInstanceScope>) : MocksRegistry, MockInstantiationListener {

    private val _mocks = MutableMocksCollection(mocks)
    private val lock = ReentrantLock()

    override val mocks: MocksCollection get() = lock.withLock { _mocks }

    override fun register(mock: MokkeryInstanceScope) = lock.withLock { _mocks.upsertScope(mock) }

    override fun onMockInstantiation(obj: Any, scope: MokkeryInstanceScope) = register(scope)

    override fun toString(): String = "MocksRegistry { ${mocks.scopes.joinToString()} }"
}
