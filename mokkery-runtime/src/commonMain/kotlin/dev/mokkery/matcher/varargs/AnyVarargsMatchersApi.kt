@file:OptIn(ExperimentalUnsignedTypes::class)

package dev.mokkery.matcher.varargs

import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any

/**
 * Matches any sequence of varargs.
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public inline fun <reified T> MokkeryMatcherScope.anyVarargs(): Array<T> = any()

/**
 * [anyVarargs] variant for [BooleanArray].
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public fun MokkeryMatcherScope.anyVarargsBoolean(): BooleanArray = any()

/**
 * [anyVarargs] variant for [CharArray].
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public fun MokkeryMatcherScope.anyVarargsChar(): CharArray = any()

/**
 * [anyVarargs] variant for [ByteArray].
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public fun MokkeryMatcherScope.anyVarargsByte(): ByteArray = any()

/**
 * [anyVarargs] variant for [UByteArray].
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public fun MokkeryMatcherScope.anyVarargsUByte(): UByteArray = any()

/**
 * [anyVarargs] variant for [ShortArray].
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public fun MokkeryMatcherScope.anyVarargsShort(): ShortArray = any()

/**
 * [anyVarargs] variant for [UShortArray].
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public fun MokkeryMatcherScope.anyVarargsUShort(): UShortArray = any()

/**
 * [anyVarargs] variant for [IntArray].
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public  fun MokkeryMatcherScope.anyVarargsInt(): IntArray = any()

/**
 * [anyVarargs] variant for [UIntArray].
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public fun MokkeryMatcherScope.anyVarargsUInt(): IntArray = any()

/**
 * [anyVarargs] variant for [LongArray].
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public fun MokkeryMatcherScope.anyVarargsLong(): LongArray = any()

/**
 * [anyVarargs] variant for [ULongArray].
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public fun MokkeryMatcherScope.anyVarargsULong(): ULongArray = any()

/**
 * [anyVarargs] variant for [FloatArray].
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public fun MokkeryMatcherScope.anyVarargsFloat(): FloatArray = any()

/**
 * [anyVarargs] variant for [DoubleArray].
 */
@Deprecated(OBSOLETE_VARARGS_MESSAGE, ReplaceWith("any()", "dev.mokkery.matcher.any"))
public fun MokkeryMatcherScope.anyVarargsDouble(): DoubleArray = any()
