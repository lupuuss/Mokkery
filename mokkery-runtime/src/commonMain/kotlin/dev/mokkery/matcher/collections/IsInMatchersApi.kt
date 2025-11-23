package dev.mokkery.matcher.collections

import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches

/**
 * Matches argument that is present in [values].
 */
public fun <T> MokkeryMatcherScope.isIn(
    vararg values: T
): T = matches(CollectionArgMatchers.ValueInIterable(values.toList()))

/**
 * Matches argument that is present in [values].
 */
public fun <T> MokkeryMatcherScope.isIn(
    values: Iterable<T>
): T = matches(CollectionArgMatchers.ValueInIterable(values))


/**
 * Matches argument that is not present in [values].
 */
public fun <T> MokkeryMatcherScope.isNotIn(
    vararg values: T
): T = matches(CollectionArgMatchers.ValueNotInIterable(values.toList()))

/**
 * Matches argument that is not present in [values].
 */
public fun <T> MokkeryMatcherScope.isNotIn(
    values: Iterable<T>
): T = matches(CollectionArgMatchers.ValueNotInIterable(values))
