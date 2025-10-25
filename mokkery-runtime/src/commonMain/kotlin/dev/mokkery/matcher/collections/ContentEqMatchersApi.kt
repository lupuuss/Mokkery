@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.collections

import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches


/**
 * Matches an [Array] that is equal to [array] with [contentDeepEquals].
 */
public fun <T> MokkeryMatcherScope.contentDeepEq(array: Array<T>): Array<T> {
    return matches(CollectionArgMatchers.ContentDeepEquals(array))
}

/**
 * Matches an [Array] that has the same content as [array].
 */
public fun <T> MokkeryMatcherScope.contentEq(array: Array<T>): Array<T> {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * Matches an [BooleanArray] that has the same content as [array].
 */
public fun MokkeryMatcherScope.contentEq(array: BooleanArray): BooleanArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * Matches an [CharArray] that has the same content as [array].
 */
public fun MokkeryMatcherScope.contentEq(array: CharArray): CharArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * Matches an [ByteArray] that has the same content as [array].
 */
public fun MokkeryMatcherScope.contentEq(array: ByteArray): ByteArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * Matches an [UByteArray] that has the same content as [array].
 */
public fun MokkeryMatcherScope.contentEq(array: UByteArray): UByteArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * Matches an [ShortArray] that has the same content as [array].
 */
public fun MokkeryMatcherScope.contentEq(array: ShortArray): ShortArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * Matches an [UShortArray] that has the same content as [array].
 */
public fun MokkeryMatcherScope.contentEq(array: UShortArray): UShortArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * Matches an [IntArray] that has the same content as [array].
 */
public fun MokkeryMatcherScope.contentEq(array: IntArray): IntArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * Matches an [UIntArray] that has the same content as [array].
 */
public fun MokkeryMatcherScope.contentEq(array: UIntArray): UIntArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * Matches an [LongArray] that has the same content as [array].
 */
public fun MokkeryMatcherScope.contentEq(array: LongArray): LongArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * Matches an [ULongArray] that has the same content as [array].
 */
public fun MokkeryMatcherScope.contentEq(array: ULongArray): ULongArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * Matches an [FloatArray] that has the same content as [array].
 */
public fun MokkeryMatcherScope.contentEq(array: FloatArray): FloatArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}

/**
 * Matches an [DoubleArray] that has the same content as [array].
 */
public fun MokkeryMatcherScope.contentEq(array: DoubleArray): DoubleArray {
    return matches(CollectionArgMatchers.ContentEquals(array))
}
