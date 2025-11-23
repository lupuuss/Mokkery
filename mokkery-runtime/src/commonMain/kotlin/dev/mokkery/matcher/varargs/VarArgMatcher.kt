package dev.mokkery.matcher.varargs

import dev.drewhamilton.poko.Poko
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.utils.asListOrNull
import dev.mokkery.internal.utils.capitalize
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.matcher.ArgMatcher
import kotlin.reflect.KClass

internal const val OBSOLETE_VARARGS_MESSAGE = "Obsolete varargs API. Read `VarArgMatcher` docs for more details."

/**
 * Wildcard vararg matcher that checks a subset of varargs. It can occur only once.
 * To provide your own implementation use [Base].
 *
 * **Vararg matchers are DEPRECATED**
 *
 * Since Mokkery 3, it is possible to use any matcher that accepts an array with the spread operator (*),
 * making vararg matchers obsolete. They have been replaced with matchers that are more versatile
 * and can be used with arrays in any context.
 *
 * Examples:
 *
 * ```kotlin
 * // Mokkery 2
 * every { mock.callWithVarargs(1, *anyIntVarargs(), 10) } returns 1
 * every { mock.callWithVarargs(1, *varargsIntAll { it % 2 == 0 }, 10) } returns 1
 *
 * // Mokkery 3
 * every { mock.callWithVarargs(1, *any(), 10) } returns 1
 * every { mock.callWithVarargs(1, *containsAllInts { it % 2 == 0 }, 10) } returns 1
 * ```
 *
 * The new approach allows you to easily create custom matchers and use them with the spread operator:
 * ```kotlin
 * every { mock.callWithVarargs(1, *matches { it.size > 2 }, 10) } returns 1
 * // Matches a call with varargs that starts with 1, ends with 10, and has at least two elements in between.
 * ```
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE)
@Suppress("DEPRECATION")
public sealed interface VarArgMatcher : ArgMatcher<Any?> {


    /**
     * Base class for any [VarArgMatcher]. Returns false if arg is not an array or list.
     */
    @Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("ArrayArgMatcher", "dev.mokkery.matcher.collections.ArrayArgMatcher"))
    public abstract class Base<in T> : VarArgMatcher {

        final override fun matches(arg: Any?): Boolean {
            val varargs = arg.asListOrNull() ?: return false
            return matchesVarargs(varargs.unsafeCast())
        }

        public abstract fun matchesVarargs(varargs: List<T>): Boolean
    }

    /**
     * Matches a sequence of varargs with all elements matching the [predicate].
     * @param type of vararg element to provide vararg type information in [AllThat.toString]
     */
    @DelicateMokkeryApi
    @Poko
    @Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("CollectionArgMatchers.ArrayAllMatch", "dev.mokkery.matcher.collections.CollectionArgMatchers"))
    public class AllThat<T>(private val type: KClass<*>, private val predicate: (T) -> Boolean) : Base<T>() {

        override fun matchesVarargs(varargs: List<T>): Boolean = varargs.all(predicate)

        override fun toString(): String = "${varargNameByElementType(type)}All {...}"
    }

    /**
     * Matches a sequence of varargs with any element matching the [predicate].
     * @param type of vararg element to provide vararg type information in [AnyThat.toString]
     */
    @DelicateMokkeryApi
    @Poko
    @Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("CollectionArgMatchers.ArrayAnyMatch", "dev.mokkery.matcher.collections.CollectionArgMatchers"))
    public class AnyThat<T>(private val type: KClass<*>, private val predicate: (T) -> Boolean) : Base<T>() {

        override fun matchesVarargs(varargs: List<T>): Boolean = varargs.any(predicate)

        override fun toString(): String = "${varargNameByElementType(type)}Any { ... }"
    }

    /**
     * Matches any sequence of varargs.
     * @param type of vararg element to provide vararg type information in [AnyOf.toString]
     */
    @DelicateMokkeryApi
    @Poko
    @Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("ArgMatcher.Any", "dev.mokkery.matcher.ArgMatcher"))
    public class AnyOf(private val type: KClass<*>) : Base<Any?>() {

        override fun matchesVarargs(varargs: List<Any?>): Boolean = true

        override fun toString(): String = "any${varargNameByElementType(type).capitalize()}()"
    }
}

private fun varargNameByElementType(cls: KClass<*>): String = when (cls) {
    Int::class -> "varargsInt"
    UInt::class -> "varargsUInt"
    Short::class -> "varargsShort"
    UShort::class -> "varargsUShort"
    Byte::class -> "varargsByte"
    UByte::class -> "varargsUByte"
    Char::class -> "varargsChar"
    Double::class -> "varargsDouble"
    Float::class -> "varargsFloat"
    ULong::class -> "varargsULong"
    Long::class -> "varargsLong"
    Boolean::class -> "varargsBoolean"
    Any::class -> "varargs"
    else -> "varargs<${cls.simpleName}>"
}
