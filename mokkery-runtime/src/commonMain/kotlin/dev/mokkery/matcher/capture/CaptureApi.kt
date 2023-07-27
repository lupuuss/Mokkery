@file:Suppress("UNUSED_PARAMETER")

package dev.mokkery.matcher.capture

import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches

/**
 * Matches an argument with [matcher] and captures matching arguments into [capture].
 * Arguments are captured only if all other matchers match.
 */
public inline fun <reified T> ArgMatchersScope.capture(
    container: Capture<T>,
    matcher: T = any()
): T {
    return matches(CaptureMatcher(container))
}

/**
 * Matches an argument with [matcher] and captures matching arguments into [list].
 * Arguments are captured only if all other matchers match.
 */
public inline fun <reified T> ArgMatchersScope.capture(
    list: MutableList<T>,
    matcher: T = any()
): T {
    return matches(CaptureMatcher(list.asCapture()))
}

/**
 * Short for [capture] with [Capture.callback].
 */
public inline fun <reified T> ArgMatchersScope.onArg(matcher: T = any(), noinline block: (T) -> Unit): T {
    return capture(Capture.callback(callback = block), matcher = matcher)
}
