package dev.mokkery.internal.context

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryMockInstance
import dev.mokkery.internal.utils.MocksContainer
import dev.mokkery.internal.utils.instances
import dev.mokkery.internal.utils.toMutableMocksContainer
import dev.mokkery.internal.utils.upsert
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

internal interface MocksRegistry : MokkeryContext.Element {

    override val key get() = Key

    val mocks: MocksContainer

    fun register(mock: MokkeryMockInstance)

    companion object Key : MokkeryContext.Key<MocksRegistry>
}

internal fun MocksRegistry(mocks: List<MokkeryMockInstance> = emptyList()): MocksRegistry {
    return MocksRegistryImpl(mocks)
}

private class MocksRegistryImpl(mocks: List<MokkeryMockInstance>) : MocksRegistry, MockInstantiationListener {

    private val _mocks = mocks.toMutableMocksContainer()
    private val lock = ReentrantLock()

    override val mocks: MocksContainer
        get() = lock.withLock { _mocks }

    override fun register(mock: MokkeryMockInstance) {
        lock.withLock { _mocks.upsert(mock) }
    }

    override fun onMockInstantiation(scope: MokkeryScope) {
        if (scope !is MokkeryMockInstance) return
        register(scope)
    }

    override fun toString(): String = "MocksRegistry { ${mocks.instances.joinToString()} }"
}
