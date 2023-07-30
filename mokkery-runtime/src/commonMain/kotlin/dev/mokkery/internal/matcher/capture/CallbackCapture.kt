package dev.mokkery.internal.matcher.capture

import dev.mokkery.matcher.capture.Capture

internal data class CallbackCapture<T>(
    val onArg: (T) -> Unit,
    val capture: Capture<T>,
): Capture<T> {

    override fun capture(value: T) {
        onArg(value)
        capture.capture(value)
    }

    override fun toString(): String = "callback($capture) {...}"
}
