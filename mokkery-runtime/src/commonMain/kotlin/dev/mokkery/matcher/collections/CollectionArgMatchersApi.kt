@file:Suppress("NOTHING_TO_INLINE", "FunctionName", "UnusedReceiverParameter", "unused")
@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.collections

import dev.mokkery.internal.utils.erasedMatcherCode
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.ArgMatchersScope

/**
 * Matches argument that is present in [values].
 */
public inline fun <T> ArgMatchersScope.isIn(
    vararg values: T
): T = erasedMatcherCode

/**
 * Matches argument that is present in [values].
 */
public inline fun <T> ArgMatchersScope.isIn(
    values: Iterable<T>
): T = erasedMatcherCode


/**
 * Matches argument that is not present in [values].
 */
public inline fun <T> ArgMatchersScope.isNotIn(
    vararg values: T
): T = erasedMatcherCode

/**
 * Matches argument that is not present in [values].
 */
public inline fun <T> ArgMatchersScope.isNotIn(
    values: Iterable<T>
): T = erasedMatcherCode


/**
 * Matches an array that is equal to [array] with [contentDeepEquals].
 */
public inline fun <T> ArgMatchersScope.contentDeepEq(array: Array<T>): Array<T> = erasedMatcherCode

/**
 * Matches an array that has the same content as [array].
 */
public inline fun <T> ArgMatchersScope.contentEq(array: Array<T>): Array<T> = erasedMatcherCode

/**
 * [contentEq] variant for [IntArray].
 */
public fun ArgMatchersScope.contentEq(array: IntArray): IntArray = erasedMatcherCode

/**
 * [contentEq] variant for [LongArray].
 */
public fun ArgMatchersScope.contentEq(array: LongArray): LongArray = erasedMatcherCode
/**
 * [contentEq] variant for [ShortArray].
 */
public fun ArgMatchersScope.contentEq(array: ShortArray): ShortArray = erasedMatcherCode

/**
 * [contentEq] variant for [ByteArray].
 */
public fun ArgMatchersScope.contentEq(array: ByteArray): ByteArray = erasedMatcherCode

/**
 * [contentEq] variant for [UIntArray].
 */
public fun ArgMatchersScope.contentEq(array: UIntArray): UIntArray = erasedMatcherCode

/**
 * [contentEq] variant for [ULongArray].
 */
public fun ArgMatchersScope.contentEq(array: ULongArray): ULongArray = erasedMatcherCode
/**
 * [contentEq] variant for [UShortArray].
 */
public fun ArgMatchersScope.contentEq(array: UShortArray): UShortArray = erasedMatcherCode

/**
 * [contentEq] variant for [UByteArray].
 */
public fun ArgMatchersScope.contentEq(array: UByteArray): UByteArray = erasedMatcherCode

/**
 * [contentEq] variant for [BooleanArray].
 */
public fun ArgMatchersScope.contentEq(array: BooleanArray): BooleanArray = erasedMatcherCode

/**
 * [contentEq] variant for [CharArray].
 */
public fun ArgMatchersScope.contentEq(array: CharArray): CharArray = erasedMatcherCode
/**
 * [contentEq] variant for [DoubleArray].
 */
public fun ArgMatchersScope.contentEq(array: DoubleArray): DoubleArray = erasedMatcherCode
/**
 * [contentEq] variant for [FloatArray].
 */
public fun ArgMatchersScope.contentEq(array: FloatArray): FloatArray = erasedMatcherCode

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
