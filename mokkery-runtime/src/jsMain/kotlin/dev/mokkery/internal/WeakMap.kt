package dev.mokkery.internal

internal external class WeakMap<K, V> {

    operator fun get(key: K): V?

    operator fun set(key: K, value: V)
}
