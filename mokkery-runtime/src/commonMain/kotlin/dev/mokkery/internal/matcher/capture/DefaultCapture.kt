package dev.mokkery.internal.matcher.capture

import dev.mokkery.matcher.capture.Capture

internal data class DefaultCapture<T>(
    val capturedValues: MutableList<T> = mutableListOf()
): Capture<T> {
    override val values: List<T> get() = capturedValues

    override fun capture(value: T) {
        capturedValues.add(value)
    }

    override fun toString(): String = "container(${capturedValues.joinToString()})"
}
