package dev.mokkery.internal.matcher.capture

import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.getIfPresent
import dev.mokkery.matcher.capture.isAbsent
import kotlinx.atomicfu.atomic

internal class DefaultSlotCapture<T> : SlotCapture<T> {

    private var _values by atomic(listOf<T>())
    override val values: List<T> get() = _values

    override val value get() = _values.firstOrNull()
        ?.let { SlotCapture.Value.Present(it) }
        ?: SlotCapture.Value.Absent

    override fun capture(value: T) {
        _values = listOf(value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as DefaultSlotCapture<*>

        return value == other.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String = if (isAbsent) "slot()" else "slot(value=${getIfPresent()})"
}
