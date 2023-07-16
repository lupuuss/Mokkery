@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.collections

import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.matcher.matches

/**
 * Matches argument that is present in [values].
 */
public inline fun <reified T> ArgMatchersScope.isIn(
    vararg values: T
): T = matches(CollectionArgMatchers.ValueInIterable(values.toList()))

/**
 * Matches argument that is present in [values].
 */
public inline fun <reified T> ArgMatchersScope.isIn(
    values: Iterable<T>
): T = matches(CollectionArgMatchers.ValueInIterable(values))


/**
 * Matches argument that is not present in [values].
 */
public inline fun <reified T> ArgMatchersScope.isNotIn(
    vararg values: T
): T = matches(CollectionArgMatchers.ValueNotInIterable(values.toList()))

/**
 * Matches argument that is not present in [values].
 */
public inline fun <reified T> ArgMatchersScope.isNotIn(
    values: Iterable<T>
): T = matches(CollectionArgMatchers.ValueNotInIterable(values))


/**
 * Matches an array that is equal to [array] with [contentDeepEquals].
 */
public inline fun <reified T> ArgMatchersScope.contentDeepEq(array: Array<T>): Array<T> {
    return matches(CollectionArgMatchers.ContentDeepEquals(array))
}

/**
 * Matches an array that has the same content as [array].
 */
public inline fun <reified T> ArgMatchersScope.contentEq(array: Array<T>): Array<T> {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [IntArray].
 */
public fun ArgMatchersScope.contentEq(array: IntArray): IntArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [LongArray].
 */
public fun ArgMatchersScope.contentEq(array: LongArray): LongArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [ShortArray].
 */
public fun ArgMatchersScope.contentEq(array: ShortArray): ShortArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [ByteArray].
 */
public fun ArgMatchersScope.contentEq(array: ByteArray): ByteArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [UIntArray].
 */
public fun ArgMatchersScope.contentEq(array: UIntArray): UIntArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [ULongArray].
 */
public fun ArgMatchersScope.contentEq(array: ULongArray): ULongArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [UShortArray].
 */
public fun ArgMatchersScope.contentEq(array: UShortArray): UShortArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [UByteArray].
 */
public fun ArgMatchersScope.contentEq(array: UByteArray): UByteArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [BooleanArray].
 */
public fun ArgMatchersScope.contentEq(array: BooleanArray): BooleanArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [CharArray].
 */
public fun ArgMatchersScope.contentEq(array: CharArray): CharArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [DoubleArray].
 */
public fun ArgMatchersScope.contentEq(array: DoubleArray): DoubleArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [FloatArray].
 */
public fun ArgMatchersScope.contentEq(array: FloatArray): FloatArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}
