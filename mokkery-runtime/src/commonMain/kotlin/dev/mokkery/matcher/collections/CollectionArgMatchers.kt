package dev.mokkery.matcher.collections

import dev.drewhamilton.poko.Poko
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.utils.asListOrNull
import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

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
    @Poko
    public class ContentEquals(public val array: Any): ArgMatcher<Any> {

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
        @Poko.ReadArrayContent public val array: Array<T>
    ): ArgMatcher<Array<T>> {

        override fun matches(arg: Array<T>): Boolean = arg contentDeepEquals array

        override fun toString(): String = "contentDeepEq(${array.asListOrNull().orEmpty().joinToString()})"
    }

    @Poko
    public class ContainsAnyIterable<T>(
        public val predicate: (T) -> Boolean
    ): ArgMatcher<Iterable<T>> {

        override fun matches(arg: Iterable<T>): Boolean = arg.any(predicate)

        override fun toString(): String = "containsAny(...)"
    }

    @Poko
    public class ContainsAllIterable<T>(
        public val predicate: (T) -> Boolean
    ): ArgMatcher<Iterable<T>> {

        override fun matches(arg: Iterable<T>): Boolean = arg.all(predicate)

        override fun toString(): String = "containsAll(...)"
    }

    /**
     * Matches an array that contains any element matching [predicate].
     *
     * **Important:** [elementType] must be a [KClass] of [Any] if a generic [Array] is used.
     * For other arrays, e.g., [IntArray], the element type is expected, in this case [Int].
     */
    @DelicateMokkeryApi
    @Poko
    public class ContainsAnyArray<T : Any>(
        public val elementType: KClass<*>,
        public val predicate: (T) -> Boolean
    ) : ArrayArgMatcher<T>() {

        override fun matchesElements(elements: List<T>): Boolean = elements.any(predicate)

        override fun toString(): String = "containsAny${anyNameByElement(elementType)}(...)"
    }

    /**
     * Matches an array that contains only elements matching [predicate].
     */
    @DelicateMokkeryApi
    @Poko
    public class ContainsAllArray<T : Any>(
        public val elementType: KClass<*>,
        public val predicate: (T) -> Boolean
    ) : ArrayArgMatcher<T>() {

        override fun matchesElements(elements: List<T>): Boolean = elements.all(predicate)

        override fun toString(): String = "containsAll${allNameByElement(elementType)}(...)"
    }
}

private fun allNameByElement(cls: KClass<*>): String = when (cls) {
    Boolean::class -> "Booleans"
    Char::class -> "Chars"
    Byte::class -> "Bytes"
    UByte::class -> "UBytes"
    Short::class -> "Shorts"
    UShort::class -> "UShorts"
    Int::class -> "Ints"
    UInt::class -> "UInts"
    Long::class -> "Longs"
    ULong::class -> "ULongs"
    Float::class -> "Floats"
    Double::class -> "Doubles"
    else -> "Elements"
}

private fun anyNameByElement(cls: KClass<*>): String = when (cls) {
    Boolean::class -> "Boolean"
    Char::class -> "Char"
    Byte::class -> "Byte"
    UByte::class -> "UByte"
    Short::class -> "Short"
    UShort::class -> "UShort"
    Int::class -> "Int"
    UInt::class -> "UInt"
    Long::class -> "Long"
    ULong::class -> "ULong"
    Float::class -> "Float"
    Double::class -> "Double"
    else -> "Element"
}
