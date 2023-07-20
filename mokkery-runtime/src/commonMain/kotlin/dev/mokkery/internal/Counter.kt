package dev.mokkery.internal

import kotlinx.atomicfu.atomic

internal fun interface Counter {

    fun next(): Long

    companion object {

        private val _callsClock = DelegateCounter(MonotonicCounter(Long.MIN_VALUE))
        private val _mocksCounter = DelegateCounter(MonotonicCounter(0))
        val calls: Counter = _callsClock
        val mocks: Counter = _mocksCounter
    }
}

internal class DelegateCounter(
    initial: Counter
): Counter {

    var current: Counter by atomic(initial)

    override fun next(): Long = current.next()
}

internal class MonotonicCounter(start: Long): Counter {

    private val currentStamp = atomic(start)

    override fun next(): Long = currentStamp.getAndIncrement()

}
