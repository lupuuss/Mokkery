package dev.mokkery.matcher

import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Gt
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Gte
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Lt
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Lte
import kotlin.reflect.KClass

public inline fun <reified T> ArgMatchersScope.any(): T = matches(ArgMatcher.AnyOf())

public inline fun <reified T> ArgMatchersScope.eq(value: T): T = matches(ArgMatcher.Equals(value))

public inline fun <reified T> ArgMatchersScope.notEq(value: T): T = matches(ArgMatcher.NotEqual(value))

public inline fun <reified T> ArgMatchersScope.eqRef(value: T): T = matches(ArgMatcher.EqualsRef(value))

public inline fun <reified T> ArgMatchersScope.notEqRef(value: T): T = matches(ArgMatcher.NotEqualRef(value))

public inline fun <reified T> ArgMatchersScope.matching(
    noinline predicate: (T) -> Boolean,
    noinline toString: () -> String = { "matching(...)" }
): T = matching(T::class, predicate, toString)

public inline fun <reified T> ArgMatchersScope.lt(
    value: T
): T where T : Number, T : Comparable<T> = matches(ArgMatcher.Comparing(value, Lt))

public inline fun <reified T> ArgMatchersScope.lte(
    value: T
): T where T : Number, T : Comparable<T> = matches(ArgMatcher.Comparing(value, Lte))

public inline fun <reified T> ArgMatchersScope.gt(
    value: T
): T where T : Number, T : Comparable<T> = matches(ArgMatcher.Comparing(value, Gt))

public inline fun <reified T> ArgMatchersScope.gte(
    value: T
): T where T : Number, T : Comparable<T> = matches(ArgMatcher.Comparing(value, Gte))

@PublishedApi
internal fun <T> ArgMatchersScope.matching(
    argType: KClass<*>,
    predicate: (T) -> Boolean,
    toString: () -> String = { "matching(...)" }
): T = matches(argType, ArgMatcher.Matching(predicate, toString))
