@file:Suppress("UNUSED_PARAMETER", "NOTHING_TO_INLINE", "FunctionName")

package dev.mokkery.matcher.capture

import dev.mokkery.annotations.Matcher
import dev.mokkery.internal.utils.erasedMatcherCode
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.matcher._anyMokkeryMatcher
import dev.mokkery.matcher.any

/**
 * Matches an argument with [matcher] and captures matching arguments into [capture].
 * Arguments are captured only if all other matchers match.
 */
public inline fun <T> ArgMatchersScope.capture(
    container: Capture<T>,
    @Matcher matcher: T = any()
): T = erasedMatcherCode

/**
 * Matches an argument with [matcher] and captures matching arguments into [list].
 * Arguments are captured only if all other matchers match.
 */
public inline fun <reified T> ArgMatchersScope.capture(
    list: MutableList<T>,
    @Matcher matcher: T = any()
): T = erasedMatcherCode

/**
 * Short for [capture] with [Capture.callback].
 */
public inline fun <T> ArgMatchersScope.onArg(@Matcher matcher: T = any(), noinline block: (T) -> Unit): T = erasedMatcherCode

internal inline fun <reified T> ArgMatchersScope._captureMokkeryMatcher(
    container: Capture<T>,
    matcher: ArgMatcher<T> = _anyMokkeryMatcher()
): ArgMatcher.Composite<T> = CaptureMatcher(container, matcher)

internal inline fun <reified T> ArgMatchersScope._captureMokkeryMatcher(
    list: MutableList<T>,
    matcher: ArgMatcher<T> = _anyMokkeryMatcher()
): ArgMatcher.Composite<T> = CaptureMatcher(list.asCapture(), matcher)

internal inline fun <T> ArgMatchersScope._onArgMokkeryMatcher(
    matcher: ArgMatcher<T> = _anyMokkeryMatcher(),
    noinline block: (T) -> Unit
): ArgMatcher.Composite<T> = CaptureMatcher(Capture.callback { block(it) }, matcher)
