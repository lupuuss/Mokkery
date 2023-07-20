package dev.mokkery.matcher.capture

import dev.mokkery.internal.MissingMatchersForComposite
import dev.mokkery.matcher.ArgMatcher

/**
 * Matches an argument with [matcher] and captures matching arguments into [capture].
 */
public data class CaptureMatcher<T>(
    val capture: Capture<T>,
    val matcher: ArgMatcher<T>? = null,
) : ArgMatcher.Composite<T> {

    override fun matches(arg: T): Boolean {
        if (matcher!!.matches(arg)) {
            capture.capture(arg)
            return true
        }
        return false
    }

    override fun compose(matcher: ArgMatcher<T>): ArgMatcher.Composite<T> = copy(matcher = matcher)

    override fun isFilled(): Boolean = matcher != null

    override fun assertFilled() {
        if (matcher == null) {
            throw MissingMatchersForComposite("capture", 1, listOfNotNull(matcher))
        }
    }
}
