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
    public class Equals<T>(public val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg == value

        override fun toString(): String = value.description()

        override fun equals(other: kotlin.Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Equals<*>
            return value == other.value
        }

        override fun hashCode(): Int = value?.hashCode() ?: 0
    }

    /**
     * Matches an argument that is not equal to [value].
     */
    public class NotEqual<T>(public val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg != value

        override fun toString(): String = "neq(${value.description()})"

        override fun equals(other: kotlin.Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Equals<*>
            return value == other.value
        }

        override fun hashCode(): Int = value?.hashCode() ?: 0
    }

    /**
     * Matches an argument whose reference is equal to [value]'s reference.
     */
    public class EqualsRef<T>(public val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg === value

        override fun toString(): String = "eqRef(${value.description()})"

        override fun equals(other: kotlin.Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Equals<*>
            return value == other.value
        }

        override fun hashCode(): Int = value?.hashCode() ?: 0
    }

    /**
     * Matches an argument whose reference is not equal to [value]'s reference.
     */
    public class NotEqualRef<T>(public val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg !== value

        override fun toString(): String = "neqRef(${value.description()})"

        override fun equals(other: kotlin.Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Equals<*>
            return value == other.value
        }

        override fun hashCode(): Int = value?.hashCode() ?: 0
    }

    /**
     *  Matches an argument according to the [predicate]. Registered matcher [Any.toString] calls [toStringFun].
     */
    public class Matching<T>(
        public val predicate: (T) -> Boolean,
        public val toStringFun: (() -> String)
    ) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = predicate(arg)

        override fun toString(): String = toStringFun()

        override fun equals(other: kotlin.Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Matching<*>
            if (predicate != other.predicate) return false
            if (toStringFun != other.toStringFun) return false
            return true
        }

        override fun hashCode(): Int {
            var result = predicate.hashCode()
            result = 31 * result + toStringFun.hashCode()
            return result
        }
    }

    /**
     * Matches any [Comparable] [value] depending on [type] parameter.
     */
    public class Comparing<T>(
        public val value: T,
        public val type: Type,
    ) : ArgMatcher<T> where T : Comparable<T> {
        override fun matches(arg: T): Boolean = type.compare(arg.compareTo(value))

        override fun toString(): String = "${type.toString().lowercase()}(${value.description()})"

        override fun equals(other: kotlin.Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as Comparing<*>
            if (value != other.value) return false
            if (type != other.type) return false
            return true
        }

        override fun hashCode(): Int {
            var result = value.hashCode()
            result = 31 * result + type.hashCode()
            return result
        }

        public enum class Type(public val compare: (Int) -> Boolean) {
            Eq({ it == 0 }), Lt({ it < 0 }), Lte({ it <= 0 }), Gt({ it > 0 }), Gte({ it >= 0 })
        }
    }

    /**
     * Matches an argument that is instance of [type].
     */
    public class OfType<T>(public val type: KClass<*>) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = type.isInstance(arg)

        override fun toString(): String = "ofType<${type.bestName()}>()"

        override fun equals(other: kotlin.Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as OfType<*>
            return type == other.type
        }

        override fun hashCode(): Int = type.hashCode()
    }

    /**
     * Arg matcher that must be composed with other matchers. Every composite matcher has to implement [Capture] to propagate it to its children.
     * Use [dev.mokkery.matcher.capture.propagateCapture] for convenience.
     *
     * Check existing implementations to learn how to implement it correctly
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
