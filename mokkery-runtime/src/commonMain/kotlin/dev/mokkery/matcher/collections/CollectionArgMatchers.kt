@file:OptIn(ArrayContentSupport::class)

package dev.mokkery.matcher.collections

import dev.drewhamilton.poko.ArrayContentBased
import dev.drewhamilton.poko.ArrayContentSupport
import dev.drewhamilton.poko.Poko
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.asListOrNull
import dev.mokkery.matcher.ArgMatcher

/**
 * Contains matchers for collections
 */
public object CollectionArgMatchers {

    @Poko
    public class ValueInIterable<T>(public val iterable: Iterable<T>): ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg in iterable

        override fun toString(): String = "isIn(${iterable.joinToString()})"
    }

    @Poko
    public class ValueNotInIterable<T>(public val iterable: Iterable<T>): ArgMatcher<T> {

        override fun matches(arg: T): Boolean = arg !in iterable

        override fun toString(): String = "isNotIn(${iterable.joinToString()})"
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
    @Poko
    public class ContentDeepEquals<T>(
        @ArrayContentBased public val array: Array<T>
    ): ArgMatcher<Array<T>> {

        override fun matches(arg: Array<T>): Boolean = arg contentDeepEquals array

        override fun toString(): String = "contentDeepEq(${array.asListOrNull().orEmpty().joinToString()})"
    }
}
