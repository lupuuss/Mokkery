package dev.mokkery.internal

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

internal fun <K, V> synchronizedMapOf(): MutableMap<K, V> = SynchronizedMap()

private class SynchronizedMap<K, V> : MutableMap<K, V> {

    private val lock = reentrantLock()
    private val map = mutableMapOf<K, V>()

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>>
        get() = lock.withLock { map.entries }
    override val keys: MutableSet<K>
        get() = lock.withLock { map.keys }
    override val size: Int
        get() = lock.withLock { map.size }
    override val values: MutableCollection<V>
        get() = lock.withLock { map.values }

    override fun clear() = lock.withLock { map.clear() }

    override fun isEmpty(): Boolean = lock.withLock { map.isEmpty() }

    override fun remove(key: K): V? = lock.withLock { map.remove(key) }

    override fun putAll(from: Map<out K, V>) = lock.withLock { map.putAll(from) }

    override fun put(key: K, value: V): V? = lock.withLock { map.put(key, value) }

    override fun get(key: K): V? = lock.withLock { map[key] }

    override fun containsValue(value: V): Boolean = lock.withLock { map.containsValue(value) }

    override fun containsKey(key: K): Boolean = lock.withLock { map.containsKey(key) }
}
