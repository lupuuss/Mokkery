package dev.mokkery.matcher.capture

import dev.mokkery.matcher.ArgMatcher

/**
 * Helper function to propagate capture for [dev.mokkery.matcher.ArgMatcher.Composite].
 */
public fun <T> List<ArgMatcher<T>>.propagateCapture(value: T) {
    filterIsInstance<Capture<T>>().forEach { it.capture(value) }
}

/**
 * Helper function to propagate capture for [dev.mokkery.matcher.ArgMatcher.Composite].
 */
public fun <T> ArgMatcher<T>.propagateCapture(value: T) {
    listOf(this).propagateCapture(value)
}
