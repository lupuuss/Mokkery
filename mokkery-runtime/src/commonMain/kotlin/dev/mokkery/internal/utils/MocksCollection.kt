@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.internal.utils

import dev.mokkery.internal.GlobalMokkeryScope
import dev.mokkery.internal.MockId
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.mockId
import dev.mokkery.internal.resolveInstance

internal interface MocksCollection {

    val ids: Set<MockId>

    val scopes: Collection<MokkeryInstanceScope>

    fun getScopeOrNull(id: MockId): MokkeryInstanceScope?
}

internal interface MutableMocksCollection : MocksCollection {

    fun upsertScope(scope: MokkeryInstanceScope)

    fun clear()
}

internal fun MocksCollection(values: List<MokkeryInstanceScope> = emptyList()): MocksCollection {
    return MocksCollectionImpl(values)
}

internal fun MutableMocksCollection(values: List<MokkeryInstanceScope> = emptyList()): MutableMocksCollection {
    return MocksCollectionImpl(values)
}

internal inline fun MocksCollection?.orEmpty(): MocksCollection = this ?: MocksCollection()

internal inline fun MocksCollection.getScope(id: MockId): MokkeryInstanceScope = getScopeOrNull(id)
    ?: mokkeryRuntimeError("Failed to find mock with $id!")

internal val MocksCollection.instances: List<Any>
    get() {
        val scopeLookup = GlobalMokkeryScope.tools.scopeLookup
        return scopes.map(scopeLookup::resolveInstance)
    }

internal operator fun MocksCollection.plus(other: MocksCollection): MocksCollection = MutableMocksCollection(scopes + other.scopes)

internal fun MocksCollection.forEachScope(block: (MokkeryInstanceScope) -> Unit) {
    scopes.forEach(block)
}

private class MocksCollectionImpl(
    initial: List<MokkeryInstanceScope>
) : MutableMocksCollection {

    private val map = LinkedHashMap(initial.associateBy { it.mockId })

    override val ids: Set<MockId> get() = map.keys

    override fun getScopeOrNull(id: MockId): MokkeryInstanceScope? = map[id]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MocksCollection) return false
        return this.ids == other.ids
    }

    override fun hashCode(): Int = this.ids.hashCode()

    override val scopes: MutableCollection<MokkeryInstanceScope>
        get() = map.values

    override fun upsertScope(scope: MokkeryInstanceScope) {
        map[scope.mockId] = scope
    }

    override fun clear() = map.clear()
}
