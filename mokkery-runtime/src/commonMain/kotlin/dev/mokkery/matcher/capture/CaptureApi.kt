package dev.mokkery.matcher.capture

import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.matchesComposite

/**
 * Matches an argument with [matcher] and captures matching arguments into [capture].
 * Arguments are captured only if all other matchers match.
 */
public fun <T> MokkeryMatcherScope.capture(
    container: Capture<T>,
    @Matcher matcher: T = any()
): T = matchesComposite(matcher) { CaptureMatcher(container, it[0]) }

/**
 * Matches an argument with [matcher] and captures matching arguments into [list].
 * Arguments are captured only if all other matchers match.
 */
public fun <T> MokkeryMatcherScope.capture(
    list: MutableList<T>,
    @Matcher matcher: T = any()
): T = capture(list.asCapture(), matcher)

/**
 * Short for [capture] with [Capture.callback].
 */
public fun <T> MokkeryMatcherScope.onArg(
    @Matcher matcher: T = any(),
    block: (T) -> Unit
): T = capture(Capture.callback(callback = block), matcher = matcher)
