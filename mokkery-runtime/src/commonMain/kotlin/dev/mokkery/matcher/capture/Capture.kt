package dev.mokkery.matcher.capture

import dev.mokkery.internal.matcher.capture.DebugCapture
import dev.mokkery.internal.matcher.capture.DefaultCapture
import dev.mokkery.internal.matcher.capture.DefaultSlotCapture
import dev.mokkery.internal.matcher.capture.VoidCapture

/**
 * Container for captured values. If [dev.mokkery.matcher.ArgMatcher] implements this interface,
 * [Capture.capture] is called on full match (all matchers for given call match).
 * Implementing [Capture] is not recommended. Use [CaptureMatcher].
 */
public interface Capture<T> {

    public val values: List<T>

    public fun capture(value: T)

    public companion object {

        /**
         * Creates a [SlotCapture] implementation.
         */
        public fun <T> slot(): SlotCapture<T> = DefaultSlotCapture()

        /**
         * Creates a [Capture] that is able to store multiple values.
         */
        public fun <T> container(): Capture<T> = DefaultCapture()

        /**
         * Creates a [Capture] that prints incoming values and stores them in [capture].
         * It is possible
         */
        public fun <T> debug(
            name: String? = null,
            capture: Capture<T> = container()
        ): Capture<T> = DebugCapture(name, capture)

        /**
         * Creates a [Capture] that ignores incoming values.
         */
        public fun <T> void(): Capture<T> = VoidCapture()
    }
}

/**
 * Returns a [Capture] that stores values in [this] list.
 */
public fun <T> MutableList<T>.asCapture(): Capture<T> {
    return DefaultCapture(this)
}

