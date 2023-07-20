package dev.mokkery.internal.matcher.capture

import dev.mokkery.matcher.capture.SlotCapture
import dev.mokkery.matcher.capture.getIfPresent
import dev.mokkery.matcher.capture.isAbsent

internal class DefaultSlotCapture<T> : SlotCapture<T> {

    override val values = arrayListOf<T>()

    override val value get() = if (values.isEmpty()) {
        SlotCapture.Value.Absent
    } else {
        SlotCapture.Value.Present(values.first())
    }

    override fun capture(value: T) {
        values.removeLastOrNull()
        values.add(value)
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
