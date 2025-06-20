@file:Suppress("NOTHING_TO_INLINE", "FunctionName", "UnusedReceiverParameter", "unused")

package dev.mokkery.matcher

import dev.mokkery.internal.utils.erasedMatcherCode
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Gt
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Gte
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Lt
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Lte
import kotlin.reflect.KFunction1

/**
 * Matches any argument.
 */
public inline fun <T> ArgMatchersScope.any(): T = erasedMatcherCode

/**
 * Matches an argument that is equal to [value]. It can be replaced with [value] literal.
 */
public inline fun <reified T> ArgMatchersScope.eq(value: T): T = erasedMatcherCode

/**
 * Matches an argument that is not equal to [value].
 */
public inline fun <reified T> ArgMatchersScope.neq(value: T): T = erasedMatcherCode

/**
 * Matches an argument whose reference is equal to [value]'s reference.
 */
public inline fun <T> ArgMatchersScope.eqRef(value: T): T = erasedMatcherCode

/**
 * Matches an argument whose reference is not equal to [value]'s reference.
 */
public inline fun <T> ArgMatchersScope.neqRef(value: T): T = erasedMatcherCode

/**
 * Matches an argument according to the [predicate]. Registered matcher [Any.toString] calls [toString].
 */
public inline fun <T> ArgMatchersScope.matching(
    noinline toString: () -> String = { "matching(...)" },
    noinline predicate: (T) -> Boolean,
): T = erasedMatcherCode

/**
 * Matches an argument by calling given [function]. Also, it returns function name on [Any.toString].
 */
public inline fun <T> ArgMatchersScope.matchingBy(
    function: KFunction1<T, Boolean>
): T = erasedMatcherCode


/**
 * Matches argument that is less than [value].
 */
public inline fun <T : Comparable<T>> ArgMatchersScope.lt(
    value: T
): T = erasedMatcherCode

/**
 * Matches an argument that is less than or equal to [value].
 */
public inline fun <T : Comparable<T>> ArgMatchersScope.lte(
    value: T
): T = erasedMatcherCode

/**
 * Matches argument that is greater than [value].
 */
public inline fun <T : Comparable<T>> ArgMatchersScope.gt(
    value: T
): T = erasedMatcherCode

/**
 * Matches an argument that is greater than or equal to [value].
 */
public inline fun <T : Comparable<T>> ArgMatchersScope.gte(
    value: T
): T = erasedMatcherCode

/**
 * Matches an argument that is an instance of type [T].
 */
public inline fun <reified T> ArgMatchersScope.ofType(): T = erasedMatcherCode

internal inline fun <T> ArgMatchersScope._anyMokkeryMatcher(): ArgMatcher<T> = ArgMatcher.Any

internal inline fun <T> ArgMatchersScope._eqMokkeryMatcher(value: T): ArgMatcher<T> = ArgMatcher.Equals(value)

internal inline fun <T> ArgMatchersScope._neqMokkeryMatcher(value: T): ArgMatcher<T> = ArgMatcher.NotEqual(value)

internal inline fun <T> ArgMatchersScope._eqRefMokkeryMatcher(value: T): ArgMatcher<T> = ArgMatcher.EqualsRef(value)

internal inline fun <T> ArgMatchersScope._neqRefMokkeryMatcher(value: T): ArgMatcher<T> = ArgMatcher.NotEqualRef(value)

internal inline fun <T> ArgMatchersScope._matchingMokkeryMatcher(
    noinline toString: () -> String = { "matching(...)" },
    noinline predicate: (T) -> Boolean,
): ArgMatcher<T> = ArgMatcher.Matching(predicate, toString)

internal inline fun <T> ArgMatchersScope._matchingByMokkeryMatcher(
    function: KFunction1<T, Boolean>
): ArgMatcher<T> = ArgMatcher.Matching(function) { "${function.name}()" }

internal inline fun <T : Comparable<T>> ArgMatchersScope._ltMokkeryMatcher(
    value: T
): ArgMatcher<T> = ArgMatcher.Comparing(value, Lt)

internal inline fun <T : Comparable<T>> ArgMatchersScope._lteMokkeryMatcher(
    value: T
): ArgMatcher<T> = ArgMatcher.Comparing(value, Lte)

internal inline fun <T : Comparable<T>> ArgMatchersScope._gtMokkeryMatcher(
    value: T
): ArgMatcher<T> = ArgMatcher.Comparing(value, Gt)

internal inline fun <T : Comparable<T>> ArgMatchersScope._gteMokkeryMatcher(
    value: T
): ArgMatcher<T> = ArgMatcher.Comparing(value, Gte)

internal inline fun <reified T> ArgMatchersScope._ofTypeMokkeryMatcher(): ArgMatcher<T> = ArgMatcher.OfType<T>(T::class)
