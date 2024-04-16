package dev.mokkery.matcher.collections

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.asListOrNull
import dev.mokkery.matcher.ArgMatcher

/**
 * Contains matchers for collections
 */
public object CollectionArgMatchers {

    public class ValueInIterable<T>(public val iterable: Iterable<T>): ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg in iterable

        override fun toString(): String = "isIn(${iterable.joinToString()})"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as ValueInIterable<*>
            return iterable == other.iterable
        }

        override fun hashCode(): Int = iterable.hashCode()
    }

    public class ValueNotInIterable<T>(public val iterable: Iterable<T>): ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg !in iterable

        override fun toString(): String = "isNotIn(${iterable.joinToString()})"

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as ValueNotInIterable<*>
            return iterable == other.iterable
        }

        override fun hashCode(): Int = iterable.hashCode()
    }

    /**
     * Matches an array that has the same content as [array].
     * It accepts [Any] for convenience and should only receive arrays.
     */
    @DelicateMokkeryApi
    public class ContentEquals(private val array: Any): ArgMatcher<Any> {

        private val elements = requireNotNull(array.asListOrNull()) {
            "ContentEquals expects array but received $array!"
        }

        override fun matches(arg: Any): Boolean {
            val actual = arg.asListOrNull() ?: return false
            return actual == elements
        }

        override fun toString(): String = "contentEq(${array.asListOrNull().orEmpty().joinToString()})"
    }

    /**
     * Matches an array that is equal to [array] with [contentDeepEquals].
     */
    public class ContentDeepEquals<T>(public val array: Array<T>): ArgMatcher<Array<T>> {

        override fun matches(arg: Array<T>): Boolean = arg contentDeepEquals array

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false
            other as ContentDeepEquals<*>
            return array.contentEquals(other.array)
        }

        override fun hashCode(): Int = array.contentHashCode()

        override fun toString(): String = "contentDeepEq(${array.asListOrNull().orEmpty().joinToString()})"

    }
}
