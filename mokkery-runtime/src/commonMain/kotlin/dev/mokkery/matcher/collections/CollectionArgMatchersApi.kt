package dev.mokkery.matcher.collections

import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.matcher.matches

/**
 * Matches argument that is present in [values].
 */
public inline fun <reified T> ArgMatchersScope.isIn(
    vararg values: T
): T = matches(CollectionArgMatchers.ValueInIterable(values.toList()))

/**
 * Matches argument that is present in [values].
 */
public inline fun <reified T> ArgMatchersScope.isIn(
    values: Iterable<T>
): T = matches(CollectionArgMatchers.ValueInIterable(values))


/**
 * Matches argument that is not present in [values].
 */
public inline fun <reified T> ArgMatchersScope.isNotIn(
    vararg values: T
): T = matches(CollectionArgMatchers.ValueNotInIterable(values.toList()))

/**
 * Matches argument that is not present in [values].
 */
public inline fun <reified T> ArgMatchersScope.isNotIn(
    values: Iterable<T>
): T = matches(CollectionArgMatchers.ValueNotInIterable(values))
