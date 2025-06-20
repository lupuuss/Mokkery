@file:Suppress("NOTHING_TO_INLINE", "FunctionName", "UnusedReceiverParameter", "unused")
@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.varargs

import dev.mokkery.internal.utils.generatedCode
import dev.mokkery.matcher.ArgMatchersScope

/**
 * Matches a sequence of varargs with all elements satisfying the given [predicate].
 */
public inline fun <reified T> ArgMatchersScope.varargsAll(
    noinline predicate: (T) -> Boolean
): Array<T> = generatedCode

/**
 * [varargsAll] variant for [LongArray].
 */
public inline fun ArgMatchersScope.varargsLongAll(
    noinline predicate: (Long) -> Boolean
): LongArray = generatedCode

/**
 * [varargsAll] variant for [IntArray].
 */
public inline fun ArgMatchersScope.varargsIntAll(
    noinline predicate: (Int) -> Boolean
): IntArray = generatedCode


/**
 * [varargsAll] variant for [ShortArray].
 */
public inline fun ArgMatchersScope.varargsShortAll(
    noinline predicate: (Short) -> Boolean
): ShortArray = generatedCode

/**
 * [varargsAll] variant for [ByteArray].
 */
public inline fun ArgMatchersScope.varargsByteAll(
    noinline predicate: (Byte) -> Boolean
): ByteArray = generatedCode

/**
 * [varargsAll] variant for [CharArray].
 */
public inline fun ArgMatchersScope.varargsCharAll(
    noinline predicate: (Char) -> Boolean
): CharArray = generatedCode

/**
 * [varargsAll] variant for [BooleanArray].
 */
public inline fun ArgMatchersScope.varargsBooleanAll(
    noinline predicate: (Boolean) -> Boolean
): BooleanArray = generatedCode

/**
 * [varargsAll] variant for [ULongArray].
 */
public inline fun ArgMatchersScope.varargsULongAll(
    noinline predicate: (ULong) -> Boolean
): ULongArray = generatedCode

/**
 * [varargsAll] variant for [UIntArray].
 */
public inline fun ArgMatchersScope.varargsUIntAll(
    noinline predicate: (UInt) -> Boolean
): IntArray = generatedCode

/**
 * [varargsAll] variant for [UShortArray].
 */
public inline fun ArgMatchersScope.varargsUShortAll(
    noinline predicate: (UShort) -> Boolean
): UShortArray = generatedCode

/**
 * [varargsAll] variant for [UByteArray].
 */
public inline fun ArgMatchersScope.varargsUByteAll(
    noinline predicate: (UByte) -> Boolean
): UByteArray = generatedCode

/**
 * [varargsAll] variant for [DoubleArray].
 */
public inline fun ArgMatchersScope.varargsDoubleAll(
    noinline predicate: (Double) -> Boolean
): DoubleArray = generatedCode

/**
 * [varargsAll] variant for [FloatArray].
 */
public inline fun ArgMatchersScope.varargsFloatAll(
    noinline predicate: (Float) -> Boolean
): FloatArray = generatedCode

/**
 * Matches a sequence of varargs with any element satisfying the given [predicate].
 */
public inline fun <reified T> ArgMatchersScope.varargsAny(
    noinline predicate: (T) -> Boolean
): Array<T> = generatedCode

/**
 * [varargsAny] variant for [LongArray].
 */
public inline fun ArgMatchersScope.varargsLongAny(
    noinline predicate: (Long) -> Boolean
): LongArray = generatedCode

/**
 * [varargsAny] variant for [IntArray].
 */
public inline fun ArgMatchersScope.varargsIntAny(
    noinline predicate: (Int) -> Boolean
): IntArray = generatedCode

/**
 * [varargsAny] variant for [ShortArray].
 */
public inline fun ArgMatchersScope.varargsShortAny(
    noinline predicate: (Short) -> Boolean
): ShortArray = generatedCode

/**
 * [varargsAny] variant for [ByteArray].
 */
public inline fun ArgMatchersScope.varargsByteAny(
    noinline predicate: (Byte) -> Boolean
): ByteArray = generatedCode

/**
 * [varargsAny] variant for [CharArray].
 */
public inline fun ArgMatchersScope.varargsCharAny(
    noinline predicate: (Char) -> Boolean
): CharArray = generatedCode

/**
 * [varargsAny] variant for [BooleanArray].
 */
public inline fun ArgMatchersScope.varargsBooleanAny(
    noinline predicate: (Boolean) -> Boolean
): BooleanArray = generatedCode

/**
 * [varargsAny] variant for [ULongArray].
 */
public inline fun ArgMatchersScope.varargsULongAny(
    noinline predicate: (ULong) -> Boolean
): ULongArray = generatedCode

/**
 * [varargsAny] variant for [UIntArray].
 */
public inline fun ArgMatchersScope.varargsUIntAny(
    noinline predicate: (UInt) -> Boolean
): IntArray = generatedCode

/**
 * [varargsAny] variant for [UShortArray].
 */
public inline fun ArgMatchersScope.varargsUShortAny(
    noinline predicate: (UShort) -> Boolean
): UShortArray = generatedCode

/**
 * [varargsAny] variant for [UByteArray].
 */
public inline fun ArgMatchersScope.varargsUByteAny(
    noinline predicate: (UByte) -> Boolean
): UByteArray = generatedCode

/**
 * [varargsAny] variant for [DoubleArray].
 */
public inline fun ArgMatchersScope.varargsDoubleAny(
    noinline predicate: (Double) -> Boolean
): DoubleArray = generatedCode

/**
 * [varargsAny] variant for [FloatArray].
 */
public inline fun ArgMatchersScope.varargsFloatAny(
    noinline predicate: (Float) -> Boolean
): FloatArray = generatedCode

/**
 * Matches any sequence of varargs.
 */
public inline fun <reified T> ArgMatchersScope.anyVarargs(): Array<T> = generatedCode

/**
 * [anyVarargs] variant for [LongArray].
 */
public inline fun ArgMatchersScope.anyVarargsLong(): LongArray = generatedCode

/**
 * [anyVarargs] variant for [IntArray].
 */
public inline fun ArgMatchersScope.anyVarargsInt(): IntArray = generatedCode

/**
 * [anyVarargs] variant for [ShortArray].
 */
public inline fun ArgMatchersScope.anyVarargsShort(): ShortArray = generatedCode

/**
 * [anyVarargs] variant for [ByteArray].
 */
public inline fun ArgMatchersScope.anyVarargsByte(): ByteArray = generatedCode

/**
 * [anyVarargs] variant for [CharArray].
 */
public inline fun ArgMatchersScope.anyVarargsChar(): CharArray = generatedCode

/**
 * [anyVarargs] variant for [BooleanArray].
 */
public inline fun ArgMatchersScope.anyVarargsBoolean(): BooleanArray = generatedCode

/**
 * [anyVarargs] variant for [ULongArray].
 */
public inline fun ArgMatchersScope.anyVarargsULong(): ULongArray = generatedCode

/**
 * [anyVarargs] variant for [UIntArray].
 */
public inline fun ArgMatchersScope.anyVarargsUInt(): IntArray = generatedCode

/**
 * [anyVarargs] variant for [UShortArray].
 */
public inline fun ArgMatchersScope.anyVarargsUShort(): UShortArray = generatedCode

/**
 * [anyVarargs] variant for [UByteArray].
 */
public inline fun ArgMatchersScope.anyVarargsUByte(): UByteArray = generatedCode

/**
 * [anyVarargs] variant for [DoubleArray].
 */
public inline fun ArgMatchersScope.anyVarargsDouble(): DoubleArray = generatedCode

/**
 * [anyVarargs] variant for [FloatArray].
 */
public inline fun ArgMatchersScope.anyVarargsFloat(): FloatArray = generatedCode


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
