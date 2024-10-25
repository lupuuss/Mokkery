package dev.mokkery.internal

import kotlinx.atomicfu.atomic

internal fun interface Counter {

    fun next(): Long
}

internal class MonotonicCounter(start: Long): Counter {

    private val current = atomic(start)

    override fun next(): Long = current.getAndIncrement()

}
