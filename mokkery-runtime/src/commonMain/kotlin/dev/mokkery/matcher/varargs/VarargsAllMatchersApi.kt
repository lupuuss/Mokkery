@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.varargs

import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.collections.containsAllBooleans
import dev.mokkery.matcher.collections.containsAllBytes
import dev.mokkery.matcher.collections.containsAllChars
import dev.mokkery.matcher.collections.containsAllDoubles
import dev.mokkery.matcher.collections.containsAllElements
import dev.mokkery.matcher.collections.containsAllFloats
import dev.mokkery.matcher.collections.containsAllInts
import dev.mokkery.matcher.collections.containsAllLongs
import dev.mokkery.matcher.collections.containsAllShorts
import dev.mokkery.matcher.collections.containsAllUBytes
import dev.mokkery.matcher.collections.containsAllUInts
import dev.mokkery.matcher.collections.containsAllULongs
import dev.mokkery.matcher.collections.containsAllUShorts

/**
 * Matches a sequence of varargs with all elements satisfying the given [predicate].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllElements(predicate)", "dev.mokkery.matcher.collections.containsAllElements")
)
public inline fun <reified T> MokkeryMatcherScope.varargsAll(
    noinline predicate: (T) -> Boolean
): Array<T> = containsAllElements(predicate)

/**
 * [varargsAll] variant for [BooleanArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllBooleans(predicate)", "dev.mokkery.matcher.collections.containsAllBooleans")
)
public fun MokkeryMatcherScope.varargsBooleanAll(
    predicate: (Boolean) -> Boolean
): BooleanArray = containsAllBooleans(predicate)

/**
 * [varargsAll] variant for [CharArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllChars(predicate)", "dev.mokkery.matcher.collections.containsAllChars")
)
public fun MokkeryMatcherScope.varargsCharAll(
    predicate: (Char) -> Boolean
): CharArray = containsAllChars(predicate)

/**
 * [varargsAll] variant for [ByteArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllBytes(predicate)", "dev.mokkery.matcher.collections.containsAllBytes")
)
public fun MokkeryMatcherScope.varargsByteAll(
    predicate: (Byte) -> Boolean
): ByteArray = containsAllBytes(predicate)

/**
 * [varargsAll] variant for [UByteArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllUBytes(predicate)", "dev.mokkery.matcher.collections.containsAllUBytes")
)
public fun MokkeryMatcherScope.varargsUByteAll(
    predicate: (UByte) -> Boolean
): UByteArray = containsAllUBytes(predicate)

/**
 * [varargsAll] variant for [ShortArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllShorts(predicate)", "dev.mokkery.matcher.collections.containsAllShorts")
)
public fun MokkeryMatcherScope.varargsShortAll(
    predicate: (Short) -> Boolean
): ShortArray = containsAllShorts(predicate)

/**
 * [varargsAll] variant for [UShortArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllUShorts(predicate)", "dev.mokkery.matcher.collections.containsAllUShorts")
)
public fun MokkeryMatcherScope.varargsUShortAll(
    predicate: (UShort) -> Boolean
): UShortArray = containsAllUShorts(predicate)

/**
 * [varargsAll] variant for [IntArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllInts(predicate)", "dev.mokkery.matcher.collections.containsAllInts")
)
public fun MokkeryMatcherScope.varargsIntAll(
    predicate: (Int) -> Boolean
): IntArray = containsAllInts(predicate)

/**
 * [varargsAll] variant for [UIntArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllUInts(predicate)", "dev.mokkery.matcher.collections.containsAllUInts")
)
public fun MokkeryMatcherScope.varargsUIntAll(
    predicate: (UInt) -> Boolean
): UIntArray = containsAllUInts(predicate)

/**
 * [varargsAll] variant for [LongArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllLongs(predicate)", "dev.mokkery.matcher.collections.containsAllLongs")
)
public fun MokkeryMatcherScope.varargsLongAll(
    predicate: (Long) -> Boolean
): LongArray = containsAllLongs(predicate)

/**
 * [varargsAll] variant for [ULongArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllULongs(predicate)", "dev.mokkery.matcher.collections.containsAllULongs")
)
public fun MokkeryMatcherScope.varargsULongAll(
    predicate: (ULong) -> Boolean
): ULongArray = containsAllULongs(predicate)

/**
 * [varargsAll] variant for [FloatArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllFloats(predicate)", "dev.mokkery.matcher.collections.containsAllFloats")
)
public fun MokkeryMatcherScope.varargsFloatAll(
    predicate: (Float) -> Boolean
): FloatArray = containsAllFloats(predicate)

/**
 * [varargsAll] variant for [DoubleArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAllDoubles(predicate)", "dev.mokkery.matcher.collections.containsAllDoubles")
)
public fun MokkeryMatcherScope.varargsDoubleAll(
    predicate: (Double) -> Boolean
): DoubleArray = containsAllDoubles(predicate)
