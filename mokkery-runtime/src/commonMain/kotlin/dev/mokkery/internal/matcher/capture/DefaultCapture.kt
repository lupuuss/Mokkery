package dev.mokkery.internal.matcher.capture

import dev.mokkery.matcher.capture.ContainerCapture

internal data class DefaultContainerCapture<T>(
    val capturedValues: MutableList<T> = mutableListOf()
): ContainerCapture<T> {
    override val values: List<T> get() = capturedValues

    override fun capture(value: T) {
        capturedValues.add(value)
    }

    override fun toString(): String = "container(${capturedValues.joinToString()})"
}
