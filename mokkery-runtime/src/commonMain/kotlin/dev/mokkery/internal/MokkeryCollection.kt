@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.internal

import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.utils.mokkeryRuntimeError

internal interface MokkeryCollection {

    val ids: Set<MokkeryInstanceId>

    val scopes: Collection<MokkeryInstanceScope>

    fun getScopeOrNull(id: MokkeryInstanceId): MokkeryInstanceScope?
}

internal interface MutableMokkeryCollection : MokkeryCollection {

    fun upsertScope(scope: MokkeryInstanceScope)

    fun clear()
}

internal fun MokkeryCollection(
    vararg values: MokkeryInstanceScope
): MokkeryCollection = MokkeryCollection(values.asList())

internal fun MutableMokkeryCollection(
    vararg values: MokkeryInstanceScope
): MutableMokkeryCollection = MutableMokkeryCollection(values.asList())

internal fun MokkeryCollection(
    values: List<MokkeryInstanceScope>
): MokkeryCollection = MokkeryCollectionImpl(values)

internal fun MutableMokkeryCollection(
    values: List<MokkeryInstanceScope>
): MutableMokkeryCollection = MokkeryCollectionImpl(values)

internal inline fun MokkeryCollection?.orEmpty(): MokkeryCollection = this ?: MokkeryCollection()

internal inline fun MokkeryCollection.getScope(id: MokkeryInstanceId): MokkeryInstanceScope = getScopeOrNull(id)
    ?: mokkeryRuntimeError("Failed to find mock with $id!")

internal val MokkeryCollection.instances: List<Any>
    get() = scopes.map { it.instanceSpec.thisRef }

internal operator fun MokkeryCollection.plus(
    other: MokkeryCollection
): MokkeryCollection = MutableMokkeryCollection(scopes + other.scopes)

internal fun MokkeryCollection.forEachScope(block: (MokkeryInstanceScope) -> Unit) {
    scopes.forEach(block)
}

internal fun List<MokkeryInstanceScope>.toMokkeryCollection() = MokkeryCollection(this)

internal fun MokkeryInstanceScope.wrapInMokkeryCollection() = listOf(this).toMokkeryCollection()

private class MokkeryCollectionImpl(
    initial: List<MokkeryInstanceScope>
) : MutableMokkeryCollection {

    private val map = LinkedHashMap(initial.associateBy { it.instanceId })

    override val ids: Set<MokkeryInstanceId> get() = map.keys

    override fun getScopeOrNull(id: MokkeryInstanceId): MokkeryInstanceScope? = map[id]

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MokkeryCollection) return false
        return this.ids == other.ids
    }

    override fun hashCode(): Int = this.ids.hashCode()

    override val scopes: MutableCollection<MokkeryInstanceScope>
        get() = map.values

    override fun upsertScope(scope: MokkeryInstanceScope) {
        map[scope.instanceId] = scope
    }

    override fun clear() = map.clear()
}
