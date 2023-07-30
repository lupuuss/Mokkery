package dev.mokkery.matcher

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.bestName
import dev.mokkery.internal.description
import dev.mokkery.matcher.capture.Capture
import kotlin.reflect.KClass

/**
 * Checks if given argument satisfies provided conditions.
 */
public fun interface ArgMatcher<in T> {

    public fun matches(arg: T): Boolean

    /**
     * Matches any argument.
     */
    public object Any : ArgMatcher<kotlin.Any?> {

        override fun matches(arg: kotlin.Any?): Boolean = true

        override fun toString(): String = "any()"
    }

    /**
     * Matches an argument that is equal to [value].
     */
    public data class Equals<T>(val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg == value

        override fun toString(): String = value.description()
    }

    /**
     * Matches an argument that is not equal to [value].
     */
    public data class NotEqual<T>(val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg != value

        override fun toString(): String = "neq(${value.description()})"
    }

    /**
     * Matches an argument whose reference is equal to [value]'s reference.
     */
    public data class EqualsRef<T>(val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg === value

        override fun toString(): String = "eqRef(${value.description()})"
    }

    /**
     * Matches an argument whose reference is not equal to [value]'s reference.
     */
    public data class NotEqualRef<T>(val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg !== value

        override fun toString(): String = "neqRef(${value.description()})"
    }

    /**
     *  Matches an argument according to the [predicate]. Registered matcher [Any.toString] calls [toStringFun].
     */
    public data class Matching<T>(
        val predicate: (T) -> Boolean,
        val toStringFun: (() -> String)
    ) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = predicate(arg)

        override fun toString(): String = toStringFun()
    }

    /**
     * Matches any [Comparable] [value] depending on [type] parameter.
     */
    public data class Comparing<T>(
        val value: T,
        val type: Type,
    ) : ArgMatcher<T> where T : Comparable<T> {
        override fun matches(arg: T): Boolean = type.compare(arg.compareTo(value))

        override fun toString(): String = "${type.toString().lowercase()}(${value.description()})"

        public enum class Type(public val compare: (Int) -> Boolean) {
            Eq({ it == 0 }), Lt({ it < 0 }), Lte({ it <= 0 }), Gt({ it > 0 }), Gte({ it >= 0 })
        }
    }

    /**
     * Matches an argument that is instance of [type].
     */
    public data class OfType<T>(val type: KClass<*>) : ArgMatcher<T> {
        override fun matches(arg: T): Boolean = type.isInstance(arg)

        override fun toString(): String = "ofType<${type.bestName()}>()"
    }

    /**
     * Arg matcher that must be composed with other matchers. Check existing implementations to learn
     * how to implement it correctly. Every composite matcher has to implement [Capture] to propagate it to nested
     * its children. Use [dev.mokkery.matcher.capture.propagateCapture] for convenience.
     */
    @DelicateMokkeryApi
    public interface Composite<T> : ArgMatcher<T>, Capture<T> {

        /**
         * Returns new [Composite] with [matcher] merged. This method gets matchers in reversed order.
         */
        public fun compose(matcher: ArgMatcher<T>): Composite<T>

        /**
         * Returns true if it is merged with all required matchers and must not be merged anymore.
         */
        public fun isFilled(): Boolean

        /**
         * Checks if is it is properly filled and throws exception if it is not.
         * It is called when composite is considered "final". It is often used to verify missing matchers.
         */
        public fun assertFilled()

        /**
         * Propagates [value] to children matchers.
         */
        override fun capture(value: T)
    }

}
