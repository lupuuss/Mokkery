@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.varargs

import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.collections.containsAnyElement
import dev.mokkery.matcher.collections.containsAnyBoolean
import dev.mokkery.matcher.collections.containsAnyByte
import dev.mokkery.matcher.collections.containsAnyChar
import dev.mokkery.matcher.collections.containsAnyDouble
import dev.mokkery.matcher.collections.containsAnyFloat
import dev.mokkery.matcher.collections.containsAnyInt
import dev.mokkery.matcher.collections.containsAnyLong
import dev.mokkery.matcher.collections.containsAnyShort
import dev.mokkery.matcher.collections.containsAnyUByte
import dev.mokkery.matcher.collections.containsAnyUInt
import dev.mokkery.matcher.collections.containsAnyULong
import dev.mokkery.matcher.collections.containsAnyUShort

/**
 * Matches a sequence of varargs with any element satisfying the given [predicate].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyArray(predicate)", "dev.mokkery.matcher.collections.containsAnyArray")
)
public inline fun <reified T> MokkeryMatcherScope.varargsAny(
    noinline predicate: (T) -> Boolean
): Array<T> = containsAnyElement(predicate)


/**
 * [varargsAny] variant for [BooleanArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyBoolean(predicate)", "dev.mokkery.matcher.collections.containsAnyBoolean")
)
public fun MokkeryMatcherScope.varargsBooleanAny(
    predicate: (Boolean) -> Boolean
): BooleanArray = containsAnyBoolean(predicate)

/**
 * [varargsAny] variant for [CharArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyChar(predicate)", "dev.mokkery.matcher.collections.containsAnyChar")
)
public fun MokkeryMatcherScope.varargsCharAny(
    predicate: (Char) -> Boolean
): CharArray = containsAnyChar(predicate)

/**
 * [varargsAny] variant for [ByteArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyByte(predicate)", "dev.mokkery.matcher.collections.containsAnyByte")
)
public fun MokkeryMatcherScope.varargsByteAny(
    predicate: (Byte) -> Boolean
): ByteArray = containsAnyByte(predicate)

/**
 * [varargsAny] variant for [UByteArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyUByte(predicate)", "dev.mokkery.matcher.collections.containsAnyUByte")
)
public fun MokkeryMatcherScope.varargsUByteAny(
    predicate: (UByte) -> Boolean
): UByteArray = containsAnyUByte(predicate)

/**
 * [varargsAny] variant for [ShortArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyShort(predicate)", "dev.mokkery.matcher.collections.containsAnyShort")
)
public fun MokkeryMatcherScope.varargsShortAny(
    predicate: (Short) -> Boolean
): ShortArray = containsAnyShort(predicate)

/**
 * [varargsAny] variant for [UShortArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyUShort(predicate)", "dev.mokkery.matcher.collections.containsAnyUShort")
)
public fun MokkeryMatcherScope.varargsUShortAny(
    predicate: (UShort) -> Boolean
): UShortArray = containsAnyUShort(predicate)

/**
 * [varargsAny] variant for [IntArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyInt(predicate)", "dev.mokkery.matcher.collections.containsAnyInt")
)
public fun MokkeryMatcherScope.varargsIntAny(
    predicate: (Int) -> Boolean
): IntArray = containsAnyInt(predicate)

/**
 * [varargsAny] variant for [UIntArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyUInt(predicate)", "dev.mokkery.matcher.collections.containsAnyUInt")
)
public fun MokkeryMatcherScope.varargsUIntAny(
    predicate: (UInt) -> Boolean
): UIntArray = containsAnyUInt(predicate)

/**
 * [varargsAny] variant for [LongArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyLong(predicate)", "dev.mokkery.matcher.collections.containsAnyLong")
)
public fun MokkeryMatcherScope.varargsLongAny(
    predicate: (Long) -> Boolean
): LongArray = containsAnyLong(predicate)

/**
 * [varargsAny] variant for [ULongArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyULong(predicate)", "dev.mokkery.matcher.collections.containsAnyULong")
)
public fun MokkeryMatcherScope.varargsULongAny(
    predicate: (ULong) -> Boolean
): ULongArray = containsAnyULong(predicate)

/**
 * [varargsAny] variant for [FloatArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyFloat(predicate)", "dev.mokkery.matcher.collections.containsAnyFloat")
)
public fun MokkeryMatcherScope.varargsFloatAny(
    predicate: (Float) -> Boolean
): FloatArray = containsAnyFloat(predicate)

/**
 * [varargsAny] variant for [DoubleArray].
 */
@Deprecated(
    OBSOLETE_VARARGS_MESSAGE,
    ReplaceWith("containsAnyDouble(predicate)", "dev.mokkery.matcher.collections.containsAnyDouble")
)
public fun MokkeryMatcherScope.varargsDoubleAny(
    predicate: (Double) -> Boolean
): DoubleArray = containsAnyDouble(predicate)
