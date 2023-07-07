package dev.mokkery.matcher

import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Gt
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Gte
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Lt
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Lte
import kotlin.reflect.KClass

/**
 * Matches any argument.
 */
public inline fun <reified T> ArgMatchersScope.any(): T = matches(ArgMatcher.Any)

/**
 * Matches an argument that is equal to [value]. It can be replaced with [value] literal.
 */
public inline fun <reified T> ArgMatchersScope.eq(value: T): T = matches(ArgMatcher.Equals(value))

/**
 * Matches an argument that is not equal to [value].
 */
public inline fun <reified T> ArgMatchersScope.neq(value: T): T = matches(ArgMatcher.NotEqual(value))

/**
 * Matches an argument whose reference is equal to [value]'s reference.
 */
public inline fun <reified T> ArgMatchersScope.eqRef(value: T): T = matches(ArgMatcher.EqualsRef(value))

/**
 * Matches an argument whose reference is not equal to [value]'s reference.
 */
public inline fun <reified T> ArgMatchersScope.neqRef(value: T): T = matches(ArgMatcher.NotEqualRef(value))

/**
 * Matches an argument according to the [predicate]. Registered matcher [Any.toString] calls [toString].
 */
public inline fun <reified T> ArgMatchersScope.matching(
    noinline toString: () -> String = { "matching(...)" },
    noinline predicate: (T) -> Boolean,
): T = matching(T::class, predicate, toString)

/**
 * Matches argument that is less than [value].
 */
public inline fun <reified T : Comparable<T>> ArgMatchersScope.lt(
    value: T
): T = matches(ArgMatcher.Comparing(value, Lt))

/**
 * Matches an argument that is less than or equal to [value].
 */
public inline fun <reified T : Comparable<T>> ArgMatchersScope.lte(
    value: T
): T = matches(ArgMatcher.Comparing(value, Lte))

/**
 * Matches argument that is greater than [value].
 */
public inline fun <reified T : Comparable<T>> ArgMatchersScope.gt(
    value: T
): T = matches(ArgMatcher.Comparing(value, Gt))

/**
 * Matches an argument that is greater than or equal to [value].
 */
public inline fun <reified T : Comparable<T>> ArgMatchersScope.gte(
    value: T
): T = matches(ArgMatcher.Comparing(value, Gte))

@PublishedApi
internal fun <T> ArgMatchersScope.matching(
    argType: KClass<*>,
    predicate: (T) -> Boolean,
    toString: () -> String,
): T = matches(argType, ArgMatcher.Matching(predicate, toString))
