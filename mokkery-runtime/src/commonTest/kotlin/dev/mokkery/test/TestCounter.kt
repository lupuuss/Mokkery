package dev.mokkery.test

import dev.mokkery.internal.Counter
import kotlinx.atomicfu.atomic

class TestCounter(
    private val start: Long,
) : Counter {

    private val _field = atomic(start)

    var value: Long
        get() = _field.value
        set(value) {
            _field.value = value
        }

    override fun next(): Long = _field.getAndIncrement()

    override fun reset() {
        _field.value = start
    }
}
