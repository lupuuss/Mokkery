package dev.mokkery.matcher

public inline fun <reified T> ArgMatchersScope.any(): T = matches(ArgMatcher.AnyOf())

public inline fun <reified T> ArgMatchersScope.eq(value: T): T = matches(ArgMatcher.Equals(value))

public inline fun <reified T> ArgMatchersScope.notEq(value: T): T = matches(ArgMatcher.NotEqual(value))

public inline fun <reified T> ArgMatchersScope.eqRef(value: T): T = matches(ArgMatcher.EqualsRef(value))

public inline fun <reified T> ArgMatchersScope.notEqRef(value: T): T = matches(ArgMatcher.NotEqualRef(value))

public inline fun <reified T> ArgMatchersScope.matching(
    noinline predicate: (T) -> Boolean
): T = matches(ArgMatcher.Matching(predicate))

public inline fun <reified T> ArgMatchersScope.lt(
    value: T
): T where T : Number, T : Comparable<T> = matches(ArgMatcher.Comparing(value, ArgMatcher.Comparing.Type.Lt))

public inline fun <reified T> ArgMatchersScope.lte(
    value: T
): T where T : Number, T : Comparable<T> = matches(ArgMatcher.Comparing(value, ArgMatcher.Comparing.Type.Lte))

public inline fun <reified T> ArgMatchersScope.gt(
    value: T
): T where T : Number, T : Comparable<T> {
    return matches(ArgMatcher.Comparing(value, ArgMatcher.Comparing.Type.Gt))
}

public inline fun <reified T> ArgMatchersScope.gte(
    value: T
): T where T : Number, T : Comparable<T> = matches(ArgMatcher.Comparing(value, ArgMatcher.Comparing.Type.Gte))

