@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.collections

import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches

/**
 * Matches an [Iterable] with all elements matching [predicate]
 */
public fun <T, R : Iterable<T>> MokkeryMatcherScope.containsAll(
    predicate: (T) -> Boolean
): R = matches(CollectionArgMatchers.ContainsAllIterable(predicate))

/**
 * Matches an [Array] with all elements matching [predicate]
 */
public fun <T> MokkeryMatcherScope.containsAllElements(
    predicate: (T) -> Boolean
): Array<T> = matches(CollectionArgMatchers.ContainsAllArray(Any::class, predicate))
/**
 * Matches a [BooleanArray] with all elements matching [predicate]
 */
public fun MokkeryMatcherScope.containsAllBooleans(
    predicate: (Boolean) -> Boolean
): BooleanArray = matches(CollectionArgMatchers.ContainsAllArray(Boolean::class, predicate))

/**
 * Matches a [CharArray] with all elements matching [predicate]
 */
public fun MokkeryMatcherScope.containsAllChars(
    predicate: (Char) -> Boolean
): CharArray = matches(CollectionArgMatchers.ContainsAllArray(Char::class, predicate))
/**
 * Matches a [ByteArray] with all elements matching [predicate]
 */
public fun MokkeryMatcherScope.containsAllBytes(
    predicate: (Byte) -> Boolean
): ByteArray = matches(CollectionArgMatchers.ContainsAllArray(Byte::class, predicate))

/**
 * Matches a [UByteArray] with all elements matching [predicate]
 */
public fun MokkeryMatcherScope.containsAllUBytes(
    predicate: (UByte) -> Boolean
): UByteArray = matches(CollectionArgMatchers.ContainsAllArray(UByte::class, predicate))

/**
 * Matches a [ShortArray] with all elements matching [predicate]
 */
public fun MokkeryMatcherScope.containsAllShorts(
    predicate: (Short) -> Boolean
): ShortArray = matches(CollectionArgMatchers.ContainsAllArray(Short::class, predicate))

/**
 * Matches a [UShortArray] with all elements matching [predicate]
 */
public fun MokkeryMatcherScope.containsAllUShorts(
    predicate: (UShort) -> Boolean
): UShortArray = matches(CollectionArgMatchers.ContainsAllArray(UShort::class, predicate))

/**
 * Matches a [IntArray] with all elements matching [predicate]
 */
public fun MokkeryMatcherScope.containsAllInts(
    predicate: (Int) -> Boolean
): IntArray = matches(CollectionArgMatchers.ContainsAllArray(Int::class, predicate))

/**
 * Matches a [UIntArray] with all elements matching [predicate]
 */
public fun MokkeryMatcherScope.containsAllUInts(
    predicate: (UInt) -> Boolean
): UIntArray = matches(CollectionArgMatchers.ContainsAllArray(UInt::class, predicate))

/**
 * Matches a [LongArray] with all elements matching [predicate]
 */
public fun MokkeryMatcherScope.containsAllLongs(
    predicate: (Long) -> Boolean
): LongArray = matches(CollectionArgMatchers.ContainsAllArray(Long::class, predicate))

/**
 * Matches a [ULongArray] with all elements matching [predicate]
 */
public fun MokkeryMatcherScope.containsAllULongs(
    predicate: (ULong) -> Boolean
): ULongArray = matches(CollectionArgMatchers.ContainsAllArray(ULong::class, predicate))

/**
 * Matches a [FloatArray] with all elements matching [predicate]
 */
public fun MokkeryMatcherScope.containsAllFloats(
    predicate: (Float) -> Boolean
): FloatArray = matches(CollectionArgMatchers.ContainsAllArray(Float::class, predicate))

/**
 * Matches a [DoubleArray] with all elements matching [predicate]
 */
public fun MokkeryMatcherScope.containsAllDoubles(
    predicate: (Double) -> Boolean
): DoubleArray = matches(CollectionArgMatchers.ContainsAllArray(Double::class, predicate))
