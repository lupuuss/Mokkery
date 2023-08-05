package dev.mokkery.plugin.core

interface Cache<K, V> {

    fun getOrPut(key: K, block: () -> V): V

    fun clear()
}

fun <K, V> Cache(): Cache<K, V> = HashMapCache()

private class HashMapCache<K, V> : Cache<K, V> {

    private val map = mutableMapOf<K, V>()
    override fun getOrPut(key: K, block: () -> V): V = map.getOrPut(key, block)

    override fun clear() = map.clear()
}
