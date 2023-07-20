package dev.mokkery.internal

import kotlinx.atomicfu.atomic

internal fun interface Counter {

    fun next(): Long

    companion object {

        private val _callsClock = MonotonicCounter(Long.MIN_VALUE)
        private val _mocksCounter = MonotonicCounter(1)
        val calls: Counter = _callsClock
        val mocks: Counter = _mocksCounter
    }
}

internal class MonotonicCounter(start: Long): Counter {

    private val current = atomic(start)

    override fun next(): Long = current.getAndIncrement()

}
