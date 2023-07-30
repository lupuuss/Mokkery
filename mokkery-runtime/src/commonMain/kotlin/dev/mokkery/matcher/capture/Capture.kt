package dev.mokkery.matcher.capture

import dev.mokkery.internal.matcher.capture.CallbackCapture
import dev.mokkery.internal.matcher.capture.DefaultContainerCapture
import dev.mokkery.internal.matcher.capture.DefaultSlotCapture
import dev.mokkery.internal.matcher.capture.VoidCapture

/**
 * Container for captured values. If [dev.mokkery.matcher.ArgMatcher] implements this interface,
 * [Capture.capture] is called on full match (all matchers for given call match).
 * Implementing [Capture] is not recommended. Use [CaptureMatcher].
 */
public interface Capture<in T> {

    public fun capture(value: T)

    public companion object {

        /**
         * Creates a [SlotCapture] implementation.
         */
        public fun <T> slot(): SlotCapture<T> = DefaultSlotCapture()

        /**
         * Creates a [Capture] that is able to store multiple values.
         */
        public fun <T> container(): ContainerCapture<T> = DefaultContainerCapture()

        /**
         * Creates a [Capture] that calls [callback] on each element and stores values in [capture] (by default it is [void]).
         */
        public fun <T> callback(
            capture: Capture<T> = void(),
            callback: (T) -> Unit,
        ): Capture<T> = CallbackCapture(callback, capture)

        /**
         * Creates a [Capture] that ignores incoming values.
         */
        public fun <T> void(): Capture<T> = VoidCapture
    }
}

/**
 * Returns a [Capture] that stores values in [this] list.
 */
public fun <T> MutableList<T>.asCapture(): Capture<T> {
    return DefaultContainerCapture(this)
}

