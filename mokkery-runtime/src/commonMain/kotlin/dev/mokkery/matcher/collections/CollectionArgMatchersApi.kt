@file:Suppress("NOTHING_TO_INLINE", "FunctionName", "UnusedReceiverParameter", "unused")
@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.collections

import dev.mokkery.internal.utils.generatedCode
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.ArgMatchersScope

/**
 * Matches argument that is present in [values].
 */
public inline fun <reified T> ArgMatchersScope.isIn(
    vararg values: T
): T = generatedCode

/**
 * Matches argument that is present in [values].
 */
public inline fun <reified T> ArgMatchersScope.isIn(
    values: Iterable<T>
): T = generatedCode


/**
 * Matches argument that is not present in [values].
 */
public inline fun <reified T> ArgMatchersScope.isNotIn(
    vararg values: T
): T = generatedCode

/**
 * Matches argument that is not present in [values].
 */
public inline fun <reified T> ArgMatchersScope.isNotIn(
    values: Iterable<T>
): T = generatedCode


/**
 * Matches an array that is equal to [array] with [contentDeepEquals].
 */
public inline fun <reified T> ArgMatchersScope.contentDeepEq(array: Array<T>): Array<T> = generatedCode

/**
 * Matches an array that has the same content as [array].
 */
public inline fun <reified T> ArgMatchersScope.contentEq(array: Array<T>): Array<T> = generatedCode

/**
 * [contentEq] variant for [IntArray].
 */
public fun ArgMatchersScope.contentEq(array: IntArray): IntArray = generatedCode

/**
 * [contentEq] variant for [LongArray].
 */
public fun ArgMatchersScope.contentEq(array: LongArray): LongArray = generatedCode
/**
 * [contentEq] variant for [ShortArray].
 */
public fun ArgMatchersScope.contentEq(array: ShortArray): ShortArray = generatedCode

/**
 * [contentEq] variant for [ByteArray].
 */
public fun ArgMatchersScope.contentEq(array: ByteArray): ByteArray = generatedCode

/**
 * [contentEq] variant for [UIntArray].
 */
public fun ArgMatchersScope.contentEq(array: UIntArray): UIntArray = generatedCode

/**
 * [contentEq] variant for [ULongArray].
 */
public fun ArgMatchersScope.contentEq(array: ULongArray): ULongArray = generatedCode
/**
 * [contentEq] variant for [UShortArray].
 */
public fun ArgMatchersScope.contentEq(array: UShortArray): UShortArray = generatedCode

/**
 * [contentEq] variant for [UByteArray].
 */
public fun ArgMatchersScope.contentEq(array: UByteArray): UByteArray = generatedCode

/**
 * [contentEq] variant for [BooleanArray].
 */
public fun ArgMatchersScope.contentEq(array: BooleanArray): BooleanArray = generatedCode

/**
 * [contentEq] variant for [CharArray].
 */
public fun ArgMatchersScope.contentEq(array: CharArray): CharArray = generatedCode
/**
 * [contentEq] variant for [DoubleArray].
 */
public fun ArgMatchersScope.contentEq(array: DoubleArray): DoubleArray = generatedCode
/**
 * [contentEq] variant for [FloatArray].
 */
public fun ArgMatchersScope.contentEq(array: FloatArray): FloatArray = generatedCode

internal inline fun <T> ArgMatchersScope._isInMokkeryMatcher(vararg values: T): ArgMatcher<T> {
    return CollectionArgMatchers.ValueInIterable(values.asList())
}

internal inline fun <T> ArgMatchersScope._isInMokkeryMatcher(values: Iterable<T>): ArgMatcher<T> {
    return CollectionArgMatchers.ValueInIterable(values)
}

internal inline fun <T> ArgMatchersScope._isNotInMokkeryMatcher(vararg values: T): ArgMatcher<T> {
    return CollectionArgMatchers.ValueNotInIterable(values.asList())
}

internal inline fun <T> ArgMatchersScope._isNotInMokkeryMatcher(values: Iterable<T>): ArgMatcher<T> {
    return CollectionArgMatchers.ValueNotInIterable(values)
}

internal inline fun <T> ArgMatchersScope._contentDeepEqMokkeryMatcher(array: Array<T>): ArgMatcher<Array<T>> {
    return CollectionArgMatchers.ContentDeepEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: Array<T>): ArgMatcher<Array<T>> {
    return CollectionArgMatchers.ContentEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: IntArray): ArgMatcher<IntArray> {
    return CollectionArgMatchers.ContentEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: LongArray): ArgMatcher<LongArray> {
    return CollectionArgMatchers.ContentEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: ShortArray): ArgMatcher<ShortArray> {
    return CollectionArgMatchers.ContentEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: ByteArray): ArgMatcher<ByteArray> {
    return CollectionArgMatchers.ContentEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: UIntArray): ArgMatcher<UIntArray> {
    return CollectionArgMatchers.ContentEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: ULongArray): ArgMatcher<ULongArray> {
    return CollectionArgMatchers.ContentEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: UShortArray): ArgMatcher<UShortArray> {
    return CollectionArgMatchers.ContentEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: UByteArray): ArgMatcher<UByteArray> {
    return CollectionArgMatchers.ContentEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: BooleanArray): ArgMatcher<BooleanArray> {
    return CollectionArgMatchers.ContentEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: CharArray): ArgMatcher<CharArray> {
    return CollectionArgMatchers.ContentEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: DoubleArray): ArgMatcher<DoubleArray> {
    return CollectionArgMatchers.ContentEquals(array)
}

internal fun <T> ArgMatchersScope._contentEqMokkeryMatcher(array: FloatArray): ArgMatcher<FloatArray> {
    return CollectionArgMatchers.ContentEquals(array)
}
