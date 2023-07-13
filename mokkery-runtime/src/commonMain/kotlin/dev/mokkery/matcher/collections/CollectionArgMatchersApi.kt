package dev.mokkery.matcher.collections

import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.matcher.matches

public inline fun <reified T> ArgMatchersScope.isIn(
    vararg values: T
): T = matches(CollectionArgMatchers.ValueInIterable(values.toList()))

public inline fun <reified T> ArgMatchersScope.isIn(
    values: Iterable<T>
): T = matches(CollectionArgMatchers.ValueInIterable(values))


public inline fun <reified T> ArgMatchersScope.isNotIn(
    vararg values: T
): T = matches(CollectionArgMatchers.ValueNotInIterable(values.toList()))

public inline fun <reified T> ArgMatchersScope.isNotIn(
    values: Iterable<T>
): T = matches(CollectionArgMatchers.ValueNotInIterable(values))
