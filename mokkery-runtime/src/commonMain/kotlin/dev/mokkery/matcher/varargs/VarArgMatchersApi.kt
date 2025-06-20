@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.varargs

import dev.mokkery.annotations.VarArgMatcherBuilder
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches

/**
 * Matches a sequence of varargs with all elements satisfying the given [predicate].
 */
@VarArgMatcherBuilder
public inline fun <reified T> MokkeryMatcherScope.varargsAll(
    noinline predicate: (T) -> Boolean
): Array<T> = varargsAllInline<T, Array<T>>(predicate)

/**
 * [varargsAll] variant for [LongArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsLongAll(
    predicate: (Long) -> Boolean
): LongArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [IntArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsIntAll(
    predicate: (Int) -> Boolean
): IntArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [ShortArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsShortAll(
    predicate: (Short) -> Boolean
): ShortArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [ByteArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsByteAll(
    predicate: (Byte) -> Boolean
): ByteArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [CharArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsCharAll(
    predicate: (Char) -> Boolean
): CharArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [BooleanArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsBooleanAll(
    predicate: (Boolean) -> Boolean
): BooleanArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [ULongArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsULongAll(
    predicate: (ULong) -> Boolean
): ULongArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [UIntArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsUIntAll(
    predicate: (UInt) -> Boolean
): IntArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [UShortArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsUShortAll(
    predicate: (UShort) -> Boolean
): UShortArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [UByteArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsUByteAll(
    predicate: (UByte) -> Boolean
): UByteArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [DoubleArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsDoubleAll(
    predicate: (Double) -> Boolean
): DoubleArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [FloatArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsFloatAll(
    predicate: (Float) -> Boolean
): FloatArray = varargsAllInline(predicate)


/**
 * Matches a sequence of varargs with any element satisfying the given [predicate].
 */
@VarArgMatcherBuilder
public inline fun <reified T> MokkeryMatcherScope.varargsAny(
    noinline predicate: (T) -> Boolean
): Array<T> = varargsAnyInline<T, Array<T>>(predicate)

/**
 * [varargsAny] variant for [LongArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsLongAny(
    predicate: (Long) -> Boolean
): LongArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [IntArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsIntAny(
    predicate: (Int) -> Boolean
): IntArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [ShortArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsShortAny(
    predicate: (Short) -> Boolean
): ShortArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [ByteArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsByteAny(
    predicate: (Byte) -> Boolean
): ByteArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [CharArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsCharAny(
    predicate: (Char) -> Boolean
): CharArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [BooleanArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsBooleanAny(
    predicate: (Boolean) -> Boolean
): BooleanArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [ULongArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsULongAny(
    predicate: (ULong) -> Boolean
): ULongArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [UIntArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsUIntAny(
    predicate: (UInt) -> Boolean
): IntArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [UShortArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsUShortAny(
    predicate: (UShort) -> Boolean
): UShortArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [UByteArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsUByteAny(
    predicate: (UByte) -> Boolean
): UByteArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [DoubleArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsDoubleAny(
    predicate: (Double) -> Boolean
): DoubleArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [FloatArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.varargsFloatAny(
    predicate: (Float) -> Boolean
): FloatArray = varargsAnyInline(predicate)


/**
 * Matches any sequence of varargs.
 */
@VarArgMatcherBuilder
public inline fun <reified T> MokkeryMatcherScope.anyVarargs(): Array<T> = anyVarargsInline<T, Array<T>>()

/**
 * [anyVarargs] variant for [LongArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.anyVarargsLong(): LongArray = anyVarargsInline<Long, _>()

/**
 * [anyVarargs] variant for [IntArray].
 */
@VarArgMatcherBuilder
public  fun MokkeryMatcherScope.anyVarargsInt(): IntArray = anyVarargsInline<Int, _>()

/**
 * [anyVarargs] variant for [ShortArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.anyVarargsShort(): ShortArray = anyVarargsInline<Short, _>()

/**
 * [anyVarargs] variant for [ByteArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.anyVarargsByte(): ByteArray = anyVarargsInline<Byte, _>()

/**
 * [anyVarargs] variant for [CharArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.anyVarargsChar(): CharArray = anyVarargsInline<Char, _>()

/**
 * [anyVarargs] variant for [BooleanArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.anyVarargsBoolean(): BooleanArray = anyVarargsInline<Boolean, _>()

/**
 * [anyVarargs] variant for [ULongArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.anyVarargsULong(): ULongArray = anyVarargsInline<ULong, _>()

/**
 * [anyVarargs] variant for [UIntArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.anyVarargsUInt(): IntArray = anyVarargsInline<UInt, _>()

/**
 * [anyVarargs] variant for [UShortArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.anyVarargsUShort(): UShortArray = anyVarargsInline<UShort, _>()

/**
 * [anyVarargs] variant for [UByteArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.anyVarargsUByte(): UByteArray = anyVarargsInline<UByte, _>()

/**
 * [anyVarargs] variant for [DoubleArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.anyVarargsDouble(): DoubleArray = anyVarargsInline<Double, _>()

/**
 * [anyVarargs] variant for [FloatArray].
 */
@VarArgMatcherBuilder
public fun MokkeryMatcherScope.anyVarargsFloat(): FloatArray = anyVarargsInline<Float, _>()

@PublishedApi
@VarArgMatcherBuilder
internal inline fun <reified T, reified TArray> MokkeryMatcherScope.varargsAllInline(
    noinline predicate: (T) -> Boolean
): TArray = matches(VarArgMatcher.AllThat(T::class, predicate))

@PublishedApi
@VarArgMatcherBuilder
internal inline fun <reified T, reified TArray> MokkeryMatcherScope.varargsAnyInline(
    noinline predicate: (T) -> Boolean
): TArray = matches(VarArgMatcher.AnyThat(T::class, predicate))

@PublishedApi
@VarArgMatcherBuilder
internal inline fun <reified T, reified TArray> MokkeryMatcherScope.anyVarargsInline(): TArray {
    return matches(VarArgMatcher.AnyOf(T::class))
}
