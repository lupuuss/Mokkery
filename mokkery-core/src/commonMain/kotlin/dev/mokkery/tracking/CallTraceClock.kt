package dev.mokkery.tracking

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
    var current: CallTraceClock
): CallTraceClock {
    override fun nextStamp(): Long = current.nextStamp()
}

private class MonotonicCallTraceClock(startStamp: Long = 0): CallTraceClock {

    private var currentStamp = startStamp

    override fun nextStamp(): Long = currentStamp.also { currentStamp++ }

}
