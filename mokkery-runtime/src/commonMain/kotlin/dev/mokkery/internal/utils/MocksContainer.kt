@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.internal.utils

import dev.mokkery.internal.GlobalMokkeryScope
import dev.mokkery.internal.MokkeryMockInstance
import dev.mokkery.internal.context.reverseResolveInstance
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.mockId

internal interface MocksContainer {

    val map: Map<String, MokkeryMockInstance>
}

internal interface MutableMocksContainer : MocksContainer {

    override val map: MutableMap<String, MokkeryMockInstance>
}

internal fun List<MokkeryMockInstance>.toMocksContainer(): MocksContainer = toMutableMocksContainer()

internal fun List<MokkeryMockInstance>.toMutableMocksContainer(): MutableMocksContainer {
    return MutableMocksContainer(this)
}

internal fun MocksContainer(values: List<MokkeryMockInstance> = emptyList()): MocksContainer {
    return MutableMocksContainerImpl(values)
}

internal fun MutableMocksContainer(values: List<MokkeryMockInstance> = emptyList()): MutableMocksContainer {
    return MutableMocksContainerImpl(values)
}
internal inline fun MocksContainer?.orEmpty(): MocksContainer = this ?: MocksContainer()

internal inline val MocksContainer.instances: Collection<MokkeryMockInstance>
    get() = map.values

internal val MocksContainer.reverseResolvedInstances: List<Any>
    get() {
        val tools = GlobalMokkeryScope.tools
        return instances.map(tools::reverseResolveInstance)
    }

internal operator fun MocksContainer.get(id: String): MokkeryMockInstance? = map[id]

internal fun MocksContainer.getValue(id: String): MokkeryMockInstance = map.getValue(id)

internal operator fun MocksContainer.plus(other: MocksContainer): MocksContainer = MutableMocksContainer(map.values + other.map.values)

internal fun MocksContainer.forEach(block: (MokkeryMockInstance) -> Unit) {
    instances.forEach(block)
}

internal fun MocksContainer.first(predicate: (MokkeryMockInstance) -> Boolean = { true }) = instances.first(predicate)

internal fun MutableMocksContainer.upsert(mock: MokkeryMockInstance) {
    map[mock.mockId] = mock
}

internal inline fun MutableMocksContainer.clear() = map.clear()

private class MutableMocksContainerImpl(
    initial: List<MokkeryMockInstance>
) : MutableMocksContainer {

    override val map = LinkedHashMap(initial.associateBy { it.mockId })

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MocksContainer) return false
        return this.map.keys == other.map.keys
    }

    override fun hashCode(): Int {
        return this.map.keys.hashCode()
    }
}
