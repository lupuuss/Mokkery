package dev.mokkery.internal.context

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.utils.MocksContainer
import dev.mokkery.internal.utils.MutableMocksContainer
import dev.mokkery.internal.utils.instances
import dev.mokkery.internal.utils.toMutableMocksContainer
import dev.mokkery.internal.utils.upsert
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

internal interface MocksRegistry : MokkeryContext.Element {

    override val key get() = Key

    val mocks: MocksContainer

    fun register(mock: MokkeryInstanceScope)

    companion object Key : MokkeryContext.Key<MocksRegistry>
}

internal fun MocksRegistry(mocks: List<MokkeryInstanceScope> = emptyList()): MocksRegistry {
    return MocksRegistryImpl(mocks)
}

private class MocksRegistryImpl(mocks: List<MokkeryInstanceScope>) : MocksRegistry, MockInstantiationListener {

    private val _mocks = MutableMocksContainer(mocks)
    private val lock = ReentrantLock()

    override val mocks: MocksContainer
        get() = lock.withLock { _mocks }

    override fun register(mock: MokkeryInstanceScope) {
        lock.withLock { _mocks.upsert(mock) }
    }

    override fun onMockInstantiation(scope: MokkeryScope) {
        if (scope !is MokkeryInstanceScope) return
        register(scope)
    }

    override fun toString(): String = "MocksRegistry { ${mocks.instances.joinToString()} }"
}
