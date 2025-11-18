package dev.mokkery.matcher.capture

import dev.mokkery.matcher.ArgMatcher

/**
 * Matches an argument with [matcher] and captures arguments into [capture].
 *
 * @see [Capture]
 */
public class CaptureMatcher<T>(
    private val capture: Capture<T>,
    private val matcher: ArgMatcher<T>
) : ArgMatcher.Composite<T> {

    override fun matches(arg: T): Boolean = matcher.matches(arg)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CaptureMatcher<*>

        return matcher == other.matcher
    }

    override fun hashCode(): Int = matcher.hashCode()

    override fun toString(): String = "capture($capture, $matcher)"

    override fun capture(value: T) {
        capture.capture(value)
        matcher.propagateCapture(value)
    }
}
