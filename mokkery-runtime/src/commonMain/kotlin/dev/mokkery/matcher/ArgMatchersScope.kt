package dev.mokkery.matcher

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.annotations.Matcher
import dev.mokkery.internal.utils.generatedCode

/**
 * Scope for registering argument matchers.
 */
public interface ArgMatchersScope {

    /**
     * Registers [matcher].
     */
    public fun <T> matches(matcher: ArgMatcher<T>): T

    /**
     * Registers composite matcher.
     */
    @DelicateMokkeryApi
    public fun <T> matchesComposite(
        @Matcher vararg matchers: T,
        builder: (List<ArgMatcher<T>>) -> ArgMatcher.Composite<T>
    ): T
}

context(scope: ArgMatchersScope)
public inline fun <T, R> T.ext(block: T.() -> R): R = generatedCode
