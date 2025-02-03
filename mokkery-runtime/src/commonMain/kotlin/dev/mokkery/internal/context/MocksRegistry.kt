package dev.mokkery.internal.context

import dev.mokkery.MokkeryTestsScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import kotlinx.atomicfu.locks.ReentrantLock
import kotlinx.atomicfu.locks.withLock

internal val MokkeryTestsScope.mocksRegistry: MocksRegistry
    get() = mokkeryContext.require(MocksRegistry)

internal interface MocksRegistry : MokkeryContext.Element {

    override val key get() = Key

    val mocks: Set<Any>

    fun register(mock: Any)

    companion object Key : MokkeryContext.Key<MocksRegistry>
}

internal fun MocksRegistry(mocks: Set<Any> = emptySet()): MocksRegistry {
    return MocksRegistryImpl(mocks)
}

private class MocksRegistryImpl(mocks: Set<Any>) : MocksRegistry {

    private val _mocks = mocks.toMutableSet()
    private val lock = ReentrantLock()

    override val mocks: Set<Any>
        get() = lock.withLock { _mocks }

    override fun register(mock: Any) {
        lock.withLock { _mocks.add(mock) }
    }

    override fun toString(): String = "MocksRegistry { ${mocks.joinToString()} }"
}
