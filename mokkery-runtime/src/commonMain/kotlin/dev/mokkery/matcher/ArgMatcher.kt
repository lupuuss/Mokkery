package dev.mokkery.matcher

/**
 * Checks if given argument satisfies provided conditions.
 */
public fun interface ArgMatcher<in T> {

    public fun matches(arg: T): Boolean

    public object Any : ArgMatcher<kotlin.Any?> {

        override fun matches(arg: kotlin.Any?): Boolean = true

        override fun toString(): String = "any()"
    }

    public data class Equals<T>(val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg == value

        override fun toString(): String = value.toString()
    }

    public data class NotEqual<T>(val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg != value

        override fun toString(): String = "notEq($value)"
    }

    public data class EqualsRef<T>(val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg === value

        override fun toString(): String = "eqRef($value)"
    }

    public data class NotEqualRef<T>(val value: T) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg !== value

        override fun toString(): String = "noEqRef($value)"
    }

    public data class Matching<T>(
        val predicate: (T) -> Boolean,
        val toStringFun: (() -> String)
    ) : ArgMatcher<T> {

        override fun matches(arg: T): Boolean = predicate(arg)

        override fun toString(): String = toStringFun()
    }

    public data class Comparing<T>(
        val value: T,
        val type: Type,
    ) : ArgMatcher<T> where T : Comparable<T> {
        override fun matches(arg: T): Boolean = type.compare(arg.compareTo(value))

        override fun toString(): String = "${type.toString().lowercase()}($value)"

        public enum class Type(public val compare: (Int) -> Boolean) {
            Eq({ it == 0 }), Lt({ it < 0 }), Lte({ it <= 0 }), Gt({ it > 0 }), Gte({ it >= 0 })
        }
    }

}
