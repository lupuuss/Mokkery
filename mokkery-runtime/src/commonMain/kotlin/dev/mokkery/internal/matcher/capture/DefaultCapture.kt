package dev.mokkery.internal.matcher.capture

import dev.mokkery.matcher.capture.ContainerCapture
import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock

internal data class DefaultContainerCapture<T>(
    val capturedValues: MutableList<T> = mutableListOf()
): ContainerCapture<T> {

    private val lock = reentrantLock()

    override val values: List<T> get() = lock.withLock { capturedValues.toMutableList() }

    override fun capture(value: T) {
        lock.withLock { capturedValues.add(value) }
    }

    override fun toString(): String = "container(${capturedValues.joinToString()})"
}
