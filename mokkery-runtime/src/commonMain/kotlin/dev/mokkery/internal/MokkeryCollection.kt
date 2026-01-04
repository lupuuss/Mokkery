@file:Suppress("NOTHING_TO_INLINE")

package dev.mokkery.internal

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.internal.context.instanceSpec

internal interface MokkeryCollection {

    val ids: Set<MokkeryInstanceId>

    val scopes: Collection<MokkeryInstanceScope>

    fun getScopeOrNull(id: MokkeryInstanceId): MokkeryInstanceScope?
}

internal interface MutableMokkeryCollection : MokkeryCollection {

    fun upsertScope(scope: MokkeryInstanceScope)

    fun clear()
}

internal fun MokkeryCollection.getScope(id: MokkeryInstanceId): MokkeryInstanceScope = getScopeOrNull(id)
    ?: mokkeryRuntimeError("Failed to find mock with $id!")

internal val MokkeryCollection.instances: List<Any>
    get() = scopes.map { it.instanceSpec.thisRef }

internal fun List<MokkeryInstanceScope>.toMokkeryCollection() = MokkeryCollection(this)

internal fun MokkeryCollection(
    vararg values: MokkeryInstanceScope
): MokkeryCollection = MokkeryCollection(values.asList())

internal fun MutableMokkeryCollection(
    vararg values: MokkeryInstanceScope
): MutableMokkeryCollection = MutableMokkeryCollection(values.asList())

internal fun MokkeryInstanceScope.toMokkeryCollection(): MokkeryCollection = SingletonMokkeryCollection(this)

internal fun MokkeryCollection?.orEmpty(): MokkeryCollection = this ?: EmptyMokkeryCollection

internal operator fun MokkeryCollection.plus(
    other: MokkeryCollection
): MokkeryCollection {
    if (this.ids.isEmpty()) return other
    if (other.ids.isEmpty()) return this
    val map = LinkedHashMap<MokkeryInstanceId, MokkeryInstanceScope>(this.ids.size + other.ids.size)
    scopes.forEach { map[it.instanceId] = it }
    other.scopes.forEach { map[it.instanceId] = it }
    return MokkeryCollectionImpl(map)
}

internal fun MokkeryCollection(
    values: List<MokkeryInstanceScope>
): MokkeryCollection = when (values.size) {
    0 -> EmptyMokkeryCollection
    1 -> SingletonMokkeryCollection(values.single())
    else -> MokkeryCollectionImpl(values.associateByTo(linkedMapOf()) { it.instanceId })
}

internal fun MutableMokkeryCollection(
    values: List<MokkeryInstanceScope>
): MutableMokkeryCollection = MokkeryCollectionImpl(values.associateByTo(linkedMapOf()) { it.instanceId })

private class MokkeryCollectionImpl(
    private val map: LinkedHashMap<MokkeryInstanceId, MokkeryInstanceScope>
) : MutableMokkeryCollection {

    override val ids: Set<MokkeryInstanceId> get() = map.keys
    override val scopes: MutableCollection<MokkeryInstanceScope>
        get() = map.values

    override fun getScopeOrNull(id: MokkeryInstanceId): MokkeryInstanceScope? = map[id]

    override fun upsertScope(scope: MokkeryInstanceScope) {
        map[scope.instanceId] = scope
    }

    override fun clear() = map.clear()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MokkeryCollection) return false
        return this.ids == other.ids
    }

    override fun hashCode(): Int = this.ids.hashCode()

    override fun toString(): String = "MokkeryCollection[${ids.joinToString()}]"
}

private class SingletonMokkeryCollection(value: MokkeryInstanceScope) : MokkeryCollection {

    override val ids = setOf(value.instanceId)
    override val scopes = listOf(value)

    private inline val scope get() = scopes[0]

    override fun getScopeOrNull(id: MokkeryInstanceId): MokkeryInstanceScope? = when (scope.instanceId) {
        id -> scope
        else -> null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MokkeryCollection) return false
        return this.ids == other.ids
    }

    override fun hashCode(): Int = this.ids.hashCode()

    override fun toString(): String = "MokkeryCollection[${scope.instanceId}]"
}

private object EmptyMokkeryCollection : MokkeryCollection {
    override val ids: Set<MokkeryInstanceId>
        get() = emptySet()
    override val scopes: Collection<MokkeryInstanceScope>
        get() = emptyList()

    override fun getScopeOrNull(id: MokkeryInstanceId): MokkeryInstanceScope? = null

    override fun equals(other: Any?): Boolean = other === EmptyMokkeryCollection || (other is MokkeryCollection && other.ids.isEmpty())

    override fun hashCode(): Int = 0

    override fun toString(): String = "MokkeryCollection[]"
}
