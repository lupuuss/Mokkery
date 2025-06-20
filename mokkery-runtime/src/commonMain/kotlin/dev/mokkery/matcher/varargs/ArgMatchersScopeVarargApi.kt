@file:Suppress("NOTHING_TO_INLINE", "FunctionName", "UnusedReceiverParameter", "unused")
@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.varargs

import dev.mokkery.annotations.VarArgMatcherBuilder
import dev.mokkery.internal.utils.erasedMatcherCode
import dev.mokkery.matcher.ArgMatchersScope

/**
 * Matches a sequence of varargs with all elements satisfying the given [predicate].
 */
@VarArgMatcherBuilder
public inline fun <reified T> ArgMatchersScope.varargsAll(
    noinline predicate: (T) -> Boolean
): Array<T> = erasedMatcherCode

/**
 * [varargsAll] variant for [LongArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsLongAll(
    noinline predicate: (Long) -> Boolean
): LongArray = erasedMatcherCode

/**
 * [varargsAll] variant for [IntArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsIntAll(
    noinline predicate: (Int) -> Boolean
): IntArray = erasedMatcherCode


/**
 * [varargsAll] variant for [ShortArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsShortAll(
    noinline predicate: (Short) -> Boolean
): ShortArray = erasedMatcherCode

/**
 * [varargsAll] variant for [ByteArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsByteAll(
    noinline predicate: (Byte) -> Boolean
): ByteArray = erasedMatcherCode

/**
 * [varargsAll] variant for [CharArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsCharAll(
    noinline predicate: (Char) -> Boolean
): CharArray = erasedMatcherCode

/**
 * [varargsAll] variant for [BooleanArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsBooleanAll(
    noinline predicate: (Boolean) -> Boolean
): BooleanArray = erasedMatcherCode

/**
 * [varargsAll] variant for [ULongArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsULongAll(
    noinline predicate: (ULong) -> Boolean
): ULongArray = erasedMatcherCode

/**
 * [varargsAll] variant for [UIntArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsUIntAll(
    noinline predicate: (UInt) -> Boolean
): IntArray = erasedMatcherCode

/**
 * [varargsAll] variant for [UShortArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsUShortAll(
    noinline predicate: (UShort) -> Boolean
): UShortArray = erasedMatcherCode

/**
 * [varargsAll] variant for [UByteArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsUByteAll(
    noinline predicate: (UByte) -> Boolean
): UByteArray = erasedMatcherCode

/**
 * [varargsAll] variant for [DoubleArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsDoubleAll(
    noinline predicate: (Double) -> Boolean
): DoubleArray = erasedMatcherCode

/**
 * [varargsAll] variant for [FloatArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsFloatAll(
    noinline predicate: (Float) -> Boolean
): FloatArray = erasedMatcherCode

/**
 * Matches a sequence of varargs with any element satisfying the given [predicate].
 */
@VarArgMatcherBuilder
public inline fun <reified T> ArgMatchersScope.varargsAny(
    noinline predicate: (T) -> Boolean
): Array<T> = erasedMatcherCode

/**
 * [varargsAny] variant for [LongArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsLongAny(
    noinline predicate: (Long) -> Boolean
): LongArray = erasedMatcherCode

/**
 * [varargsAny] variant for [IntArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsIntAny(
    noinline predicate: (Int) -> Boolean
): IntArray = erasedMatcherCode

/**
 * [varargsAny] variant for [ShortArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsShortAny(
    noinline predicate: (Short) -> Boolean
): ShortArray = erasedMatcherCode

/**
 * [varargsAny] variant for [ByteArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsByteAny(
    noinline predicate: (Byte) -> Boolean
): ByteArray = erasedMatcherCode

/**
 * [varargsAny] variant for [CharArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsCharAny(
    noinline predicate: (Char) -> Boolean
): CharArray = erasedMatcherCode

/**
 * [varargsAny] variant for [BooleanArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsBooleanAny(
    noinline predicate: (Boolean) -> Boolean
): BooleanArray = erasedMatcherCode

/**
 * [varargsAny] variant for [ULongArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsULongAny(
    noinline predicate: (ULong) -> Boolean
): ULongArray = erasedMatcherCode

/**
 * [varargsAny] variant for [UIntArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsUIntAny(
    noinline predicate: (UInt) -> Boolean
): IntArray = erasedMatcherCode

/**
 * [varargsAny] variant for [UShortArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsUShortAny(
    noinline predicate: (UShort) -> Boolean
): UShortArray = erasedMatcherCode

/**
 * [varargsAny] variant for [UByteArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsUByteAny(
    noinline predicate: (UByte) -> Boolean
): UByteArray = erasedMatcherCode

/**
 * [varargsAny] variant for [DoubleArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsDoubleAny(
    noinline predicate: (Double) -> Boolean
): DoubleArray = erasedMatcherCode

/**
 * [varargsAny] variant for [FloatArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.varargsFloatAny(
    noinline predicate: (Float) -> Boolean
): FloatArray = erasedMatcherCode

/**
 * Matches any sequence of varargs.
 */
@VarArgMatcherBuilder
public inline fun <reified T> ArgMatchersScope.anyVarargs(): Array<T> = erasedMatcherCode

/**
 * [anyVarargs] variant for [LongArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.anyVarargsLong(): LongArray = erasedMatcherCode

/**
 * [anyVarargs] variant for [IntArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.anyVarargsInt(): IntArray = erasedMatcherCode

/**
 * [anyVarargs] variant for [ShortArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.anyVarargsShort(): ShortArray = erasedMatcherCode

/**
 * [anyVarargs] variant for [ByteArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.anyVarargsByte(): ByteArray = erasedMatcherCode

/**
 * [anyVarargs] variant for [CharArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.anyVarargsChar(): CharArray = erasedMatcherCode

/**
 * [anyVarargs] variant for [BooleanArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.anyVarargsBoolean(): BooleanArray = erasedMatcherCode

/**
 * [anyVarargs] variant for [ULongArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.anyVarargsULong(): ULongArray = erasedMatcherCode

/**
 * [anyVarargs] variant for [UIntArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.anyVarargsUInt(): IntArray = erasedMatcherCode

/**
 * [anyVarargs] variant for [UShortArray].
 */
public inline fun ArgMatchersScope.anyVarargsUShort(): UShortArray = erasedMatcherCode

/**
 * [anyVarargs] variant for [UByteArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.anyVarargsUByte(): UByteArray = erasedMatcherCode

/**
 * [anyVarargs] variant for [DoubleArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.anyVarargsDouble(): DoubleArray = erasedMatcherCode

/**
 * [anyVarargs] variant for [FloatArray].
 */
@VarArgMatcherBuilder
public inline fun ArgMatchersScope.anyVarargsFloat(): FloatArray = erasedMatcherCode


internal inline fun <reified T> ArgMatchersScope._varargsAllMokkeryMatcher(noinline predicate: (T) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsLongAllMokkeryMatcher(noinline predicate: (Long) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsIntAllMokkeryMatcher(noinline predicate: (Int) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsShortAllMokkeryMatcher(noinline predicate: (Short) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsByteAllMokkeryMatcher(noinline predicate: (Byte) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsCharAllMokkeryMatcher(noinline predicate: (Char) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsBooleanAllMokkeryMatcher(noinline predicate: (Boolean) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsULongAllMokkeryMatcher(noinline predicate: (ULong) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsUShortAllMokkeryMatcher(noinline predicate: (UShort) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsUIntAllMokkeryMatcher(noinline predicate: (UInt) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsUByteAllMokkeryMatcher(noinline predicate: (UByte) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsDoubleAllMokkeryMatcher(noinline predicate: (Double) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsFloatAllMokkeryMatcher(noinline predicate: (Float) -> Boolean): VarArgMatcher {
    return _varargsAllInlineMokkeryMatcher(predicate)
}

internal inline fun <reified T> ArgMatchersScope._varargsAnyMokkeryMatcher(noinline predicate: (T) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsLongAnyMokkeryMatcher(noinline predicate: (Long) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsIntAnyMokkeryMatcher(noinline predicate: (Int) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsShortAnyMokkeryMatcher(noinline predicate: (Short) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsByteAnyMokkeryMatcher(noinline predicate: (Byte) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsCharAnyMokkeryMatcher(noinline predicate: (Char) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsBooleanAnyMokkeryMatcher(noinline predicate: (Boolean) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsULongAnyMokkeryMatcher(noinline predicate: (ULong) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsUIntAnyMokkeryMatcher(noinline predicate: (UInt) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsUShortAnyMokkeryMatcher(noinline predicate: (UShort) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsUByteAnyMokkeryMatcher(noinline predicate: (UByte) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsDoubleAnyMokkeryMatcher(noinline predicate: (Double) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun ArgMatchersScope._varargsFloatAnyMokkeryMatcher(noinline predicate: (Float) -> Boolean): VarArgMatcher {
    return _varargsAnyInlineMokkeryMatcher(predicate)
}

internal inline fun <reified T> ArgMatchersScope._anyVarargsMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<T>()

internal inline fun ArgMatchersScope._anyVarargsLongMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<Long>()

internal inline fun ArgMatchersScope._anyVarargsIntMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<Int>()

internal inline fun ArgMatchersScope._anyVarargsShortMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<Short>()

internal inline fun ArgMatchersScope._anyVarargsByteMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<Byte>()

internal inline fun ArgMatchersScope._anyVarargsCharMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<Char>()

internal inline fun ArgMatchersScope._anyVarargsBooleanMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<Boolean>()

internal inline fun ArgMatchersScope._anyVarargsULongMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<ULong>()

internal inline fun ArgMatchersScope._anyVarargsUIntMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<UInt>()

internal inline fun ArgMatchersScope._anyVarargsUShortMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<UShort>()

internal inline fun ArgMatchersScope._anyVarargsUByteMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<UByte>()

internal inline fun ArgMatchersScope._anyVarargsDoubleMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<Double>()

internal inline fun ArgMatchersScope._anyVarargsFloatMokkeryMatcher() = _anyVarargsInlineMokkeryMatcher<Float>()

@PublishedApi
internal inline fun <reified T> ArgMatchersScope._varargsAllInlineMokkeryMatcher(
    noinline predicate: (T) -> Boolean
): VarArgMatcher {
    return VarArgMatcher.AllThat(T::class, predicate)
}

@PublishedApi
internal inline fun <reified T> ArgMatchersScope._varargsAnyInlineMokkeryMatcher(
    noinline predicate: (T) -> Boolean
): VarArgMatcher {
    return VarArgMatcher.AnyThat(T::class, predicate)
}

@PublishedApi
internal inline fun <reified T> ArgMatchersScope._anyVarargsInlineMokkeryMatcher(): VarArgMatcher {
    return VarArgMatcher.AnyOf(T::class)
}
