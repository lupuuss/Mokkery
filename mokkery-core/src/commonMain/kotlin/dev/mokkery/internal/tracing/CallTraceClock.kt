package dev.mokkery.internal.tracing

import kotlinx.atomicfu.atomic

internal interface CallTraceClock {

    fun nextStamp(): Long

    companion object {

        private val _currentClock = DelegateCallTraceClock(MonotonicCallTraceClock())
        val current: CallTraceClock = _currentClock

        fun setClock(clock: CallTraceClock) {
            _currentClock.current = clock
        }
    }
}

private class DelegateCallTraceClock(
    initial: CallTraceClock
): CallTraceClock {

    var current: CallTraceClock by atomic(initial)

    override fun nextStamp(): Long = current.nextStamp()
}

private class MonotonicCallTraceClock(startStamp: Long = 0): CallTraceClock {

    private val currentStamp = atomic(startStamp)

    override fun nextStamp(): Long = currentStamp.getAndIncrement()

}
