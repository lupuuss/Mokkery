@file:Suppress("NOTHING_TO_INLINE")
@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.varargs

import dev.mokkery.matcher.ArgMatchersScope

/**
 * Matches a sequence of varargs with all elements satisfying the given [predicate].
 */
public inline fun <reified T> ArgMatchersScope.varargsAll(
    noinline predicate: (T) -> Boolean
): Array<T> {
    varargsAllInline<T, Array<T>>(predicate)
    return arrayOf()
}

/**
 * [varargsAll] variant for [LongArray].
 */
public inline fun ArgMatchersScope.varargsLongAll(
    noinline predicate: (Long) -> Boolean
): LongArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [IntArray].
 */
public inline fun ArgMatchersScope.varargsIntAll(
    noinline predicate: (Int) -> Boolean
): IntArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [ShortArray].
 */
public inline fun ArgMatchersScope.varargsShortAll(
    noinline predicate: (Short) -> Boolean
): ShortArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [ByteArray].
 */
public inline fun ArgMatchersScope.varargsByteAll(
    noinline predicate: (Byte) -> Boolean
): ByteArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [CharArray].
 */
public inline fun ArgMatchersScope.varargsCharAll(
    noinline predicate: (Short) -> Boolean
): CharArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [BooleanArray].
 */
public inline fun ArgMatchersScope.varargsBooleanAll(
    noinline predicate: (Boolean) -> Boolean
): BooleanArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [ULongArray].
 */
public inline fun ArgMatchersScope.varargsULongAll(
    noinline predicate: (ULong) -> Boolean
): ULongArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [UIntArray].
 */
public inline fun ArgMatchersScope.varargsUIntAll(
    noinline predicate: (UInt) -> Boolean
): IntArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [UShortArray].
 */
public inline fun ArgMatchersScope.varargsUShortAll(
    noinline predicate: (UShort) -> Boolean
): UShortArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [UByteArray].
 */
public inline fun ArgMatchersScope.varargsUByteAll(
    noinline predicate: (UByte) -> Boolean
): UByteArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [DoubleArray].
 */
public inline fun ArgMatchersScope.varargsDoubleAll(
    noinline predicate: (Double) -> Boolean
): DoubleArray = varargsAllInline(predicate)

/**
 * [varargsAll] variant for [FloatArray].
 */
public inline fun ArgMatchersScope.varargsFloatAll(
    noinline predicate: (Float) -> Boolean
): FloatArray = varargsAllInline(predicate)


/**
 * Matches a sequence of varargs with any element satisfying the given [predicate].
 */
public inline fun <reified T> ArgMatchersScope.varargsAny(
    noinline predicate: (T) -> Boolean
): Array<T> {
    varargsAnyInline<T, Array<T>>(predicate)
    return arrayOf()
}

/**
 * [varargsAny] variant for [LongArray].
 */
public inline fun ArgMatchersScope.varargsLongAny(
    noinline predicate: (Long) -> Boolean
): LongArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [IntArray].
 */
public inline fun ArgMatchersScope.varargsIntAny(
    noinline predicate: (Int) -> Boolean
): IntArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [ShortArray].
 */
public inline fun ArgMatchersScope.varargsShortAny(
    noinline predicate: (Short) -> Boolean
): ShortArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [ByteArray].
 */
public inline fun ArgMatchersScope.varargsByteAny(
    noinline predicate: (Byte) -> Boolean
): ByteArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [CharArray].
 */
public inline fun ArgMatchersScope.varargsCharAny(
    noinline predicate: (Short) -> Boolean
): CharArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [BooleanArray].
 */
public inline fun ArgMatchersScope.varargsBooleanAny(
    noinline predicate: (Boolean) -> Boolean
): BooleanArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [ULongArray].
 */
public inline fun ArgMatchersScope.varargsULongAny(
    noinline predicate: (ULong) -> Boolean
): ULongArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [UIntArray].
 */
public inline fun ArgMatchersScope.varargsUIntAny(
    noinline predicate: (UInt) -> Boolean
): IntArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [UShortArray].
 */
public inline fun ArgMatchersScope.varargsUShortAny(
    noinline predicate: (UShort) -> Boolean
): UShortArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [UByteArray].
 */
public inline fun ArgMatchersScope.varargsUByteAny(
    noinline predicate: (UByte) -> Boolean
): UByteArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [DoubleArray].
 */
public inline fun ArgMatchersScope.varargsDoubleAny(
    noinline predicate: (Double) -> Boolean
): DoubleArray = varargsAnyInline(predicate)

/**
 * [varargsAny] variant for [FloatArray].
 */
public inline fun ArgMatchersScope.varargsFloatAny(
    noinline predicate: (Float) -> Boolean
): FloatArray = varargsAnyInline(predicate)


/**
 * Matches any sequence of varargs.
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
internal inline fun <reified T, reified TArray> ArgMatchersScope.varargsAllInline(
    noinline predicate: (T) -> Boolean
): TArray = matches(TArray::class, VarArgMatcher.AllThat(T::class, predicate))

@PublishedApi
internal inline fun <reified T, reified TArray> ArgMatchersScope.varargsAnyInline(
    noinline predicate: (T) -> Boolean
): TArray = matches(TArray::class, VarArgMatcher.AnyThat(T::class, predicate))

@PublishedApi
internal inline fun <reified T, reified TArray> ArgMatchersScope.anyVarargsInline(): TArray {
    return matches(TArray::class, VarArgMatcher.AnyOf(T::class))
}
