package dev.mokkery.matcher.capture

import dev.mokkery.matcher.ArgMatcher

public fun <T> List<ArgMatcher<T>>.propagateCapture(value: T) {
    filterIsInstance<Capture<T>>().forEach { it.capture(value) }
}

public fun <T> ArgMatcher<T>.propagateCapture(value: T) {
    listOf(this).propagateCapture(value)
}
