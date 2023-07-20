@file:Suppress("UNUSED_PARAMETER")

package dev.mokkery.matcher.capture

import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matches

/**
 * Matches an argument with [matcher] and captures matching arguments into [capture].
 */
public inline fun <reified T> ArgMatchersScope.capture(
    container: Capture<T>,
    matcher: T = any()
): T {
    return matches(CaptureMatcher(container))
}

/**
 * Matches an argument with [matcher] and captures matching arguments into [list].
 */
public inline fun <reified T> ArgMatchersScope.capture(
    list: MutableList<T>,
    matcher: T = any()
): T {
    return matches(CaptureMatcher(list.asCapture()))
}
