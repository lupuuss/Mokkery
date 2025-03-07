@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.internal.utils

import dev.mokkery.internal.GlobalMokkeryScope
import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.context.resolveInstance
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.mockId

internal interface MocksCollection {

    val ids: Set<String>

    val scopes: Collection<MokkeryInstanceScope>

    fun getScopeOrNull(id: String): MokkeryInstanceScope?
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

internal inline fun MocksCollection.getScope(id: String): MokkeryInstanceScope = getScopeOrNull(id)
    ?: mokkeryRuntimeError("Failed to find mock with $id!")

internal val MocksCollection.instances: List<Any>
    get() {
        val tools = GlobalMokkeryScope.tools
        return scopes.map(tools::resolveInstance)
    }

internal operator fun MocksCollection.plus(other: MocksCollection): MocksCollection = MutableMocksCollection(scopes + other.scopes)

internal fun MocksCollection.forEachScope(block: (MokkeryInstanceScope) -> Unit) {
    scopes.forEach(block)
}

private class MocksCollectionImpl(
    initial: List<MokkeryInstanceScope>
) : MutableMocksCollection {

    private val map = LinkedHashMap(initial.associateBy { it.mockId })

    override val ids: Set<String> get() = map.keys

    override fun getScopeOrNull(id: String): MokkeryInstanceScope? = map[id]

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
