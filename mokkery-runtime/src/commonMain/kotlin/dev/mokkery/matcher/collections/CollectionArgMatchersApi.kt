@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.collections

import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches

/**
 * Matches argument that is present in [values].
 */
public fun <T> MokkeryMatcherScope.isIn(
    vararg values: T
): T = matches(CollectionArgMatchers.ValueInIterable(values.toList()))

/**
 * Matches argument that is present in [values].
 */
public fun <T> MokkeryMatcherScope.isIn(
    values: Iterable<T>
): T = matches(CollectionArgMatchers.ValueInIterable(values))


/**
 * Matches argument that is not present in [values].
 */
public fun <T> MokkeryMatcherScope.isNotIn(
    vararg values: T
): T = matches(CollectionArgMatchers.ValueNotInIterable(values.toList()))

/**
 * Matches argument that is not present in [values].
 */
public fun <T> MokkeryMatcherScope.isNotIn(
    values: Iterable<T>
): T = matches(CollectionArgMatchers.ValueNotInIterable(values))


/**
 * Matches an array that is equal to [array] with [contentDeepEquals].
 */
public fun <T> MokkeryMatcherScope.contentDeepEq(array: Array<T>): Array<T> {
    return matches(CollectionArgMatchers.ContentDeepEquals(array))
}

/**
 * Matches an array that has the same content as [array].
 */
public fun <T> MokkeryMatcherScope.contentEq(array: Array<T>): Array<T> {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [IntArray].
 */
public fun MokkeryMatcherScope.contentEq(array: IntArray): IntArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [LongArray].
 */
public fun MokkeryMatcherScope.contentEq(array: LongArray): LongArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [ShortArray].
 */
public fun MokkeryMatcherScope.contentEq(array: ShortArray): ShortArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [ByteArray].
 */
public fun MokkeryMatcherScope.contentEq(array: ByteArray): ByteArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [UIntArray].
 */
public fun MokkeryMatcherScope.contentEq(array: UIntArray): UIntArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [ULongArray].
 */
public fun MokkeryMatcherScope.contentEq(array: ULongArray): ULongArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [UShortArray].
 */
public fun MokkeryMatcherScope.contentEq(array: UShortArray): UShortArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [UByteArray].
 */
public fun MokkeryMatcherScope.contentEq(array: UByteArray): UByteArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [BooleanArray].
 */
public fun MokkeryMatcherScope.contentEq(array: BooleanArray): BooleanArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [CharArray].
 */
public fun MokkeryMatcherScope.contentEq(array: CharArray): CharArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [DoubleArray].
 */
public fun MokkeryMatcherScope.contentEq(array: DoubleArray): DoubleArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * [contentEq] variant for [FloatArray].
 */
public fun MokkeryMatcherScope.contentEq(array: FloatArray): FloatArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}
