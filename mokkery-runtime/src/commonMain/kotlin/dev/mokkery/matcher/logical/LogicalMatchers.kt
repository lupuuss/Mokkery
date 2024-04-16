package dev.mokkery.matcher.logical

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.MissingMatchersForComposite
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.propagateCapture

/**
 * Contains composite matchers for logical operations.
 */
public object LogicalMatchers {

    /**
     * Matches argument that satisfies all the [matchers]. It must be merged with [expectedMatchers] number of [ArgMatcher]s.
     */
    @DelicateMokkeryApi
    public class And<T>(
        public val expectedMatchers: Int,
        public val matchers: List<ArgMatcher<T>> = emptyList()
    ): ArgMatcher.Composite<T> {
        override fun matches(arg: T): Boolean = matchers.all { it.matches(arg) }

        override fun compose(matcher: ArgMatcher<T>): ArgMatcher.Composite<T> {
            return And(expectedMatchers = expectedMatchers, matchers = listOf(matcher) + matchers)
        }

        override fun isFilled(): Boolean = matchers.size == expectedMatchers

        override fun assertFilled() {
            if (matchers.size < expectedMatchers) {
                throw MissingMatchersForComposite("and", expectedMatchers, matchers)
            }
        }

        override fun toString(): String = "and(${matchers.joinToString()})"

        override fun capture(value: T) {
            matchers.propagateCapture(value)
        }
    }

    /**
     * Matches argument that satisfies any matcher from [matchers].
     * It must be merged with [expectedMatchers] number of ArgMatchers.
     */
    @DelicateMokkeryApi
    public class Or<T>(
        public val expectedMatchers: Int,
        public val matchers: List<ArgMatcher<T>> = emptyList()
    ): ArgMatcher.Composite<T> {
        override fun matches(arg: T): Boolean = matchers.any { it.matches(arg) }

        override fun compose(matcher: ArgMatcher<T>): ArgMatcher.Composite<T> {
            return Or(expectedMatchers = expectedMatchers, matchers = listOf(matcher) + matchers)
        }

        override fun isFilled(): Boolean = matchers.size == expectedMatchers

        override fun assertFilled() {
            if (matchers.size < expectedMatchers) {
                throw MissingMatchersForComposite("or", expectedMatchers, matchers)
            }
        }

        override fun toString(): String = "or(${matchers.joinToString()})"

        override fun capture(value: T) {
            matchers.propagateCapture(value)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Or<*>
            if (expectedMatchers != other.expectedMatchers) return false
            if (matchers != other.matchers) return false
            return true
        }

        override fun hashCode(): Int {
            var result = expectedMatchers
            result = 31 * result + matchers.hashCode()
            return result
        }
    }

    /**
     * Matches argument that does not satisfy [matcher].
     */
    @DelicateMokkeryApi
    public class Not<T>(public val matcher: ArgMatcher<T>? = null) : ArgMatcher.Composite<T> {

        override fun matches(arg: T): Boolean = matcher!!.matches(arg).not()

        override fun compose(matcher: ArgMatcher<T>): ArgMatcher.Composite<T> = Not(matcher = matcher)

        override fun isFilled(): Boolean = matcher != null

        override fun assertFilled() {
            if (matcher == null) {
                throw MissingMatchersForComposite("not", 1, listOf())
            }
        }

        override fun toString(): String = "not($matcher)"

        override fun capture(value: T) {
            listOfNotNull(matcher).propagateCapture(value)
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Not<*>
            return matcher == other.matcher
        }

        override fun hashCode(): Int = matcher?.hashCode() ?: 0
    }
}
