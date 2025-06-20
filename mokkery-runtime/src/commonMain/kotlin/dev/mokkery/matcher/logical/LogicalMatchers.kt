package dev.mokkery.matcher.logical

import dev.drewhamilton.poko.Poko
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.propagateCapture

/**
 * Contains composite matchers for logical operations.
 */
public object LogicalMatchers {

    /**
     * Matches argument that satisfies all the [matchers].
     */
    @DelicateMokkeryApi
    @Poko
    public class And<T>(public val matchers: List<ArgMatcher<T>>): ArgMatcher.Composite<T> {
        override fun matches(arg: T): Boolean = matchers.all { it.matches(arg) }

        override fun toString(): String = "and(${matchers.joinToString()})"

        override fun capture(value: T) {
            matchers.propagateCapture(value)
        }
    }

    /**
     * Matches argument that satisfies any matcher from [matchers].
     */
    @DelicateMokkeryApi
    @Poko
    public class Or<T>(
        public val matchers: List<ArgMatcher<T>>
    ): ArgMatcher.Composite<T> {
        override fun matches(arg: T): Boolean = matchers.any { it.matches(arg) }

        override fun toString(): String = "or(${matchers.joinToString()})"

        override fun capture(value: T) {
            matchers.propagateCapture(value)
        }
    }

    /**
     * Matches argument that does not satisfy [matcher].
     */
    @DelicateMokkeryApi
    @Poko
    public class Not<T>(public val matcher: ArgMatcher<T>) : ArgMatcher.Composite<T> {

        override fun matches(arg: T): Boolean = matcher.matches(arg).not()

        override fun toString(): String = "not($matcher)"

        override fun capture(value: T) {
            matcher.propagateCapture(value)
        }
    }
}
