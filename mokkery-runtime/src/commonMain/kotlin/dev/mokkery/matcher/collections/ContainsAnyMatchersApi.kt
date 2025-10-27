@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.collections

import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches

/**
 * Matches an [Iterable] with any element matching [predicate]
 */
public fun <T, R : Iterable<T>> MokkeryMatcherScope.containsAny(
    predicate: (T) -> Boolean
): R = matches(CollectionArgMatchers.ContainsAnyIterable(predicate))

/**
 * Matches an [Array] with any element matching [predicate]
 */
public fun <T> MokkeryMatcherScope.containsAnyElement(
    predicate: (T) -> Boolean
): Array<T> = matches(CollectionArgMatchers.ContainsAnyArray(Any::class, predicate))
/**
 * Matches a [BooleanArray] with any element matching [predicate]
 */
public fun MokkeryMatcherScope.containsAnyBoolean(
    predicate: (Boolean) -> Boolean
): BooleanArray = matches(CollectionArgMatchers.ContainsAnyArray(Boolean::class, predicate))

/**
 * Matches a [CharArray] with any element matching [predicate]
 */
public fun MokkeryMatcherScope.containsAnyChar(
    predicate: (Char) -> Boolean
): CharArray = matches(CollectionArgMatchers.ContainsAnyArray(Char::class, predicate))

/**
 * Matches a [ByteArray] with any element matching [predicate]
 */
public fun MokkeryMatcherScope.containsAnyByte(
    predicate: (Byte) -> Boolean
): ByteArray = matches(CollectionArgMatchers.ContainsAnyArray(Byte::class, predicate))

/**
 * Matches a [UByteArray] with any element matching [predicate]
 */
public fun MokkeryMatcherScope.containsAnyUByte(
    predicate: (UByte) -> Boolean
): UByteArray = matches(CollectionArgMatchers.ContainsAnyArray(UByte::class, predicate))

/**
 * Matches a [ShortArray] with any element matching [predicate]
 */
public fun MokkeryMatcherScope.containsAnyShort(
    predicate: (Short) -> Boolean
): ShortArray = matches(CollectionArgMatchers.ContainsAnyArray(Short::class, predicate))

/**
 * Matches a [UShortArray] with any element matching [predicate]
 */
public fun MokkeryMatcherScope.containsAnyUShort(
    predicate: (UShort) -> Boolean
): UShortArray = matches(CollectionArgMatchers.ContainsAnyArray(UShort::class, predicate))

/**
 * Matches an [IntArray] with any element matching [predicate]
 */
public fun MokkeryMatcherScope.containsAnyInt(
    predicate: (Int) -> Boolean
): IntArray = matches(CollectionArgMatchers.ContainsAnyArray(Int::class, predicate))

/**
 * Matches a [UIntArray] with any element matching [predicate]
 */
public fun MokkeryMatcherScope.containsAnyUInt(
    predicate: (UInt) -> Boolean
): UIntArray = matches(CollectionArgMatchers.ContainsAnyArray(UInt::class, predicate))

/**
 * Matches a [LongArray] with any element matching [predicate]
 */
public fun MokkeryMatcherScope.containsAnyLong(
    predicate: (Long) -> Boolean
): LongArray = matches(CollectionArgMatchers.ContainsAnyArray(Long::class, predicate))

/**
 * Matches a [ULongArray] with any element matching [predicate]
 */
public fun MokkeryMatcherScope.containsAnyULong(
    predicate: (ULong) -> Boolean
): ULongArray = matches(CollectionArgMatchers.ContainsAnyArray(ULong::class, predicate))

/**
 * Matches a [FloatArray] with any element matching [predicate]
 */
public fun MokkeryMatcherScope.containsAnyFloat(
    predicate: (Float) -> Boolean
): FloatArray = matches(CollectionArgMatchers.ContainsAnyArray(Float::class, predicate))

/**
 * Matches a [DoubleArray] with any element matching [predicate]
 */
public fun MokkeryMatcherScope.containsAnyDouble(
    predicate: (Double) -> Boolean
): DoubleArray = matches(CollectionArgMatchers.ContainsAnyArray(Double::class, predicate))
