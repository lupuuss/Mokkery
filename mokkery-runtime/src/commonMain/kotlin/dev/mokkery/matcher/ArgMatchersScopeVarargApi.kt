@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")
@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher

public inline fun <reified T> ArgMatchersScope.varargs(
    noinline predicate: (T) -> Boolean
): Array<T> = varargsInline(predicate)

public inline fun ArgMatchersScope.varargsLong(
    noinline predicate: (Long) -> Boolean
): LongArray = varargsInline(predicate)

public inline fun ArgMatchersScope.varargsInt(
    noinline predicate: (Int) -> Boolean
): IntArray = varargsInline(predicate)

public inline fun ArgMatchersScope.varargsShort(
    noinline predicate: (Short) -> Boolean
): ShortArray = varargsInline(predicate)

public inline fun ArgMatchersScope.varargsByte(
    noinline predicate: (Byte) -> Boolean
): ByteArray = varargsInline(predicate)

public inline fun ArgMatchersScope.varargsChar(
    noinline predicate: (Short) -> Boolean
): CharArray = varargsInline(predicate)

public inline fun ArgMatchersScope.varargsBoolean(
    noinline predicate: (Boolean) -> Boolean
): BooleanArray = varargsInline(predicate)

public inline fun ArgMatchersScope.varargsULong(
    noinline predicate: (ULong) -> Boolean
): ULongArray = varargsInline(predicate)

public inline fun ArgMatchersScope.varargsUInt(
    noinline predicate: (UInt) -> Boolean
): IntArray = varargsInline(predicate)

public inline fun ArgMatchersScope.varargsUShort(
    noinline predicate: (UShort) -> Boolean
): UShortArray = varargsInline(predicate)

public inline fun ArgMatchersScope.varargsUByte(
    noinline predicate: (UByte) -> Boolean
): UByteArray = varargsInline(predicate)

public inline fun ArgMatchersScope.varargsDouble(
    noinline predicate: (Double) -> Boolean
): DoubleArray = varargsInline(predicate)

public inline fun ArgMatchersScope.varargsFloat(
    noinline predicate: (Float) -> Boolean
): FloatArray = varargsInline(predicate)

public inline fun <reified T> ArgMatchersScope.anyVarargs(): Array<T> = anyVarargsInline<T, Array<T>>()

public inline fun ArgMatchersScope.anyVarargsLong(): LongArray = anyVarargsInline<Long, _>()

public inline fun ArgMatchersScope.anyVarargsInt(): IntArray = anyVarargsInline<Int, _>()

public inline fun ArgMatchersScope.anyVarargsShort(): ShortArray = anyVarargsInline<Short, _>()

public inline fun ArgMatchersScope.anyVarargsByte(): ByteArray = anyVarargsInline<Byte, _>()

public inline fun ArgMatchersScope.anyVarargsChar(): CharArray = anyVarargsInline<Char, _>()

public inline fun ArgMatchersScope.anyVarargsBoolean(): BooleanArray = anyVarargsInline<Boolean, _>()

public inline fun ArgMatchersScope.anyVarargsULong(): ULongArray = anyVarargsInline<ULong, _>()

public inline fun ArgMatchersScope.anyVarargsUInt(): IntArray = anyVarargsInline<UInt, _>()

public inline fun ArgMatchersScope.anyVarargsUShort(): UShortArray = anyVarargsInline<UShort, _>()

public inline fun ArgMatchersScope.anyVarargsUByte(): UByteArray = anyVarargsInline<UByte, _>()

public inline fun ArgMatchersScope.anyVarargsDouble(): DoubleArray = anyVarargsInline<Double, _>()

public inline fun ArgMatchersScope.anyVarargsFloat(): FloatArray = anyVarargsInline<Float, _>()

@PublishedApi
internal inline fun <reified T, reified TArray> ArgMatchersScope.varargsInline(
    noinline predicate: (T) -> Boolean
): TArray = matches(TArray::class, VarArgMatcher.AllThat(T::class, predicate as (Any?) -> Boolean))

@PublishedApi
internal inline fun <reified T, reified TArray> ArgMatchersScope.anyVarargsInline(): TArray {
    return matches(TArray::class, VarArgMatcher.AnyOf(T::class))
}
