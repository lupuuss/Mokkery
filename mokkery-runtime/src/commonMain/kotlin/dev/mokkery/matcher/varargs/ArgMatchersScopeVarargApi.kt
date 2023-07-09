@file:Suppress("NOTHING_TO_INLINE")
@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.varargs

import dev.mokkery.internal.unsafeCast
import dev.mokkery.matcher.ArgMatchersScope

/**
 * Matches a vararg whose elements all satisfy the given [predicate].
 */
public inline fun <reified T> ArgMatchersScope.varargs(
    noinline predicate: (T) -> Boolean
): Array<T> {
    varargsInline<T, Array<T>>(predicate)
    return arrayOf()
}

/**
 * [varargs] variant for [LongArray].
 */
public inline fun ArgMatchersScope.varargsLong(
    noinline predicate: (Long) -> Boolean
): LongArray = varargsInline(predicate)

/**
 * [varargs] variant for [IntArray].
 */
public inline fun ArgMatchersScope.varargsInt(
    noinline predicate: (Int) -> Boolean
): IntArray = varargsInline(predicate)

/**
 * [varargs] variant for [ShortArray].
 */
public inline fun ArgMatchersScope.varargsShort(
    noinline predicate: (Short) -> Boolean
): ShortArray = varargsInline(predicate)

/**
 * [varargs] variant for [ByteArray].
 */
public inline fun ArgMatchersScope.varargsByte(
    noinline predicate: (Byte) -> Boolean
): ByteArray = varargsInline(predicate)

/**
 * [varargs] variant for [CharArray].
 */
public inline fun ArgMatchersScope.varargsChar(
    noinline predicate: (Short) -> Boolean
): CharArray = varargsInline(predicate)

/**
 * [varargs] variant for [BooleanArray].
 */
public inline fun ArgMatchersScope.varargsBoolean(
    noinline predicate: (Boolean) -> Boolean
): BooleanArray = varargsInline(predicate)

/**
 * [varargs] variant for [ULongArray].
 */
public inline fun ArgMatchersScope.varargsULong(
    noinline predicate: (ULong) -> Boolean
): ULongArray = varargsInline(predicate)

/**
 * [varargs] variant for [UIntArray].
 */
public inline fun ArgMatchersScope.varargsUInt(
    noinline predicate: (UInt) -> Boolean
): IntArray = varargsInline(predicate)

/**
 * [varargs] variant for [UShortArray].
 */
public inline fun ArgMatchersScope.varargsUShort(
    noinline predicate: (UShort) -> Boolean
): UShortArray = varargsInline(predicate)

/**
 * [varargs] variant for [UByteArray].
 */
public inline fun ArgMatchersScope.varargsUByte(
    noinline predicate: (UByte) -> Boolean
): UByteArray = varargsInline(predicate)

/**
 * [varargs] variant for [DoubleArray].
 */
public inline fun ArgMatchersScope.varargsDouble(
    noinline predicate: (Double) -> Boolean
): DoubleArray = varargsInline(predicate)

/**
 * [varargs] variant for [FloatArray].
 */
public inline fun ArgMatchersScope.varargsFloat(
    noinline predicate: (Float) -> Boolean
): FloatArray = varargsInline(predicate)


/**
 * Matches a vararg with any elements sequence.
 */
public inline fun <reified T> ArgMatchersScope.anyVarargs(): Array<T> {
    anyVarargsInline<T, Array<T>>()
    return arrayOf()
}

/**
 * [anyVarargs] variant for [LongArray].
 */
public inline fun ArgMatchersScope.anyVarargsLong(): LongArray = anyVarargsInline<Long, _>()

/**
 * [anyVarargs] variant for [IntArray].
 */
public inline fun ArgMatchersScope.anyVarargsInt(): IntArray = anyVarargsInline<Int, _>()

/**
 * [anyVarargs] variant for [ShortArray].
 */
public inline fun ArgMatchersScope.anyVarargsShort(): ShortArray = anyVarargsInline<Short, _>()

/**
 * [anyVarargs] variant for [ByteArray].
 */
public inline fun ArgMatchersScope.anyVarargsByte(): ByteArray = anyVarargsInline<Byte, _>()

/**
 * [anyVarargs] variant for [CharArray].
 */
public inline fun ArgMatchersScope.anyVarargsChar(): CharArray = anyVarargsInline<Char, _>()

/**
 * [anyVarargs] variant for [BooleanArray].
 */
public inline fun ArgMatchersScope.anyVarargsBoolean(): BooleanArray = anyVarargsInline<Boolean, _>()

/**
 * [anyVarargs] variant for [ULongArray].
 */
public inline fun ArgMatchersScope.anyVarargsULong(): ULongArray = anyVarargsInline<ULong, _>()

/**
 * [anyVarargs] variant for [UIntArray].
 */
public inline fun ArgMatchersScope.anyVarargsUInt(): IntArray = anyVarargsInline<UInt, _>()

/**
 * [anyVarargs] variant for [UShortArray].
 */
public inline fun ArgMatchersScope.anyVarargsUShort(): UShortArray = anyVarargsInline<UShort, _>()

/**
 * [anyVarargs] variant for [UByteArray].
 */
public inline fun ArgMatchersScope.anyVarargsUByte(): UByteArray = anyVarargsInline<UByte, _>()

/**
 * [anyVarargs] variant for [DoubleArray].
 */
public inline fun ArgMatchersScope.anyVarargsDouble(): DoubleArray = anyVarargsInline<Double, _>()

/**
 * [anyVarargs] variant for [FloatArray].
 */
public inline fun ArgMatchersScope.anyVarargsFloat(): FloatArray = anyVarargsInline<Float, _>()

@PublishedApi
internal inline fun <reified T, reified TArray> ArgMatchersScope.varargsInline(
    noinline predicate: (T) -> Boolean
): TArray = matches(TArray::class, VarArgMatcher.AllThat(T::class, predicate.unsafeCast()))

@PublishedApi
internal inline fun <reified T, reified TArray> ArgMatchersScope.anyVarargsInline(): TArray {
    return matches(TArray::class, VarArgMatcher.AnyOf(T::class))
}
