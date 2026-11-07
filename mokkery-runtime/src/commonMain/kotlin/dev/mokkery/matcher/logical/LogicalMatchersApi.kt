package dev.mokkery.matcher.logical

import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matches
import dev.mokkery.matcher.materialize

/**
 * Matches argument that satisfies all the matchers ([first], [second] and all from [moreMatchers]).
 *
 * Example:
 * ```kotlin
 * // matches every `getForIndex` with arg that is in range 1..4
 * every { dependency.getForIndex(and(gte(1), lte(4))) }
 * ```
 */
public fun <T> MokkeryMatcherScope.and(
    @Matcher first: T,
    @Matcher second: T,
    @Matcher vararg moreMatchers: T
): T = matches(LogicalMatchers.And(materialize(first, second, *moreMatchers)))

/**
 * Matches argument that satisfies any matcher ([first], [second] or any from [moreMatchers]).
 *
 * ```kotlin
 * // matches every `getForIndex` with arg that is equal 2 or 4
 * every { dependency.getForIndex(or(1, 4)) }
 * ```
 */
public fun <T> MokkeryMatcherScope.or(
    @Matcher first: T,
    @Matcher second: T,
    @Matcher vararg moreMatchers: T
): T = matches(LogicalMatchers.Or(materialize(first, second, *moreMatchers)))

/**
 * Matches argument that does not satisfy all given matchers ([matcher] and [moreMatchers]).
 */
public fun <T> MokkeryMatcherScope.not(
    @Matcher matcher: T,
    @Matcher vararg moreMatchers: T,
): T = matches(LogicalMatchers.Not(materialize(matcher, *moreMatchers)))
