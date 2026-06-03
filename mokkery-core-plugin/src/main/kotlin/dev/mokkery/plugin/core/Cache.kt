package dev.mokkery.plugin.core

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

interface CacheStore : MokkeryContext.Element {

    override val key: MokkeryContext.Key<*> get() = Key

    operator fun <K, V> get(key: Cache.Key<K, V>): Cache<K, V>

    companion object Key : MokkeryContext.Key<CacheStore>
}

context(scope: MokkeryPluginScope)
val caches: CacheStore
    get() = scope.mokkeryContext.require(CacheStore)

fun CacheStore(): CacheStore = object : CacheStore {

    private val caches = mutableMapOf<Cache.Key<*, *>, Cache<*, *>>()

    @Suppress("UNCHECKED_CAST")
    override fun <K, V> get(key: Cache.Key<K, V>): Cache<K, V> {
        return caches.getOrPut(key) { Cache<K, V>() } as Cache<K, V>
    }
}

interface Cache<K, V> {

    operator fun get(key: K): V?

    operator fun set(key: K, value: V)

    fun clear()

    interface Key<K, V> {
        val name: String
    }
}

fun <K, V> Cache<K, V>.getOrPut(key: K, defaultValue: () -> V): V {
    return when (val value = get(key)) {
        null -> {
            val newValue = defaultValue()
            set(key, newValue)
            newValue
        }
        else -> value
    }
}

fun <K, V> Cache(): Cache<K, V> = HashMapCache()

fun <K, V> cacheKey() = PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Cache.Key<K, V>>> { _, property ->
    val key = CacheKeyImpl<K, V>(property.name)
    ReadOnlyProperty { _, _ -> key }
}

private class CacheKeyImpl<K, V>(override val name: String) : Cache.Key<K, V> {
    override fun toString(): String = "Cache.Key(name='$name')"
}

private class HashMapCache<K, V> : Cache<K, V> {

    private val map = mutableMapOf<K, V>()
    override fun get(key: K): V? = map[key]

    override fun set(key: K, value: V) {
        map[key] = value
    }

    override fun clear() = map.clear()
}
