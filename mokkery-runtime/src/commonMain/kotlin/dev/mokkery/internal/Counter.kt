package dev.mokkery.internal

import kotlinx.atomicfu.atomic

internal interface Counter {

    fun next(): Long

    fun reset()
}

internal class MonotonicCounter(private val start: Long): Counter {

    private val current = atomic(start)

    override fun next(): Long = current.getAndIncrement()

    override fun reset() {
        current.value = start
    }
}
