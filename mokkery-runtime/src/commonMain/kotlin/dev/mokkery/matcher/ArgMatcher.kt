package dev.mokkery.matcher

import kotlin.reflect.KClass

public fun interface ArgMatcher<in T> {
    public fun matches(arg: T): Boolean

    public data class AnyOf<T>(private val type: KClass<*>) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = true

        override fun toString(): String = "any(${type.simpleName})"
    }

    public data class Equals<T>(private val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg == value

        override fun toString(): String = value.toString()
    }

    public data class NotEqual<T>(private val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg != value

        override fun toString(): String = "notEq($value)"
    }

    public data class EqualsRef<T>(private val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg === value

        override fun toString(): String = "eqRef($value)"
    }

    public data class NotEqualRef<T>(private val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg === value

        override fun toString(): String = "noEqRef($value)"
    }

    public class Matching<T>(
        private val type: KClass<*>,
        private val predicate: (T) -> Boolean
    ) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = predicate(arg)

        override fun toString(): String = "matching(${type.simpleName})"
    }

    public class Comparing<T>(
        private val value: T,
        private val type: Type,
    ) : ArgMatcher<T> where T : Number, T : Comparable<T> {
        override fun matches(arg: T): Boolean = type.compare(arg.compareTo(value))

        override fun toString(): String = "${type.toString().lowercase()}($value)"

        public enum class Type(public val compare: (Int) -> Boolean) {
            Eq({ it == 0 }), Lt({ it < 0 }), Lte({ it <= 0 }), Gt({ it > 0 }), Gte({ it >= 0 })
        }
    }

}
