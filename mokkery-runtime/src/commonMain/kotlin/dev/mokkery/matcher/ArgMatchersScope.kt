package dev.mokkery.matcher

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.annotations.Matcher

/**
 * Scope for declaring argument matchers.
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
