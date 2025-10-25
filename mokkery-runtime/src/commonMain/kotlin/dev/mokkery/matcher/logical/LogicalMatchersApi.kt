package dev.mokkery.matcher.logical

import dev.mokkery.annotations.Matcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.matchesComposite

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
): T = matchesComposite(first, second, *moreMatchers, builder = LogicalMatchers::And)

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
): T = matchesComposite(first, second, *moreMatchers, builder = LogicalMatchers::Or)

/**
 * Matches argument that does not satisfy [matcher].
 */
public fun <T> MokkeryMatcherScope.not(
    @Matcher matcher: T
): T = matchesComposite(matcher) { LogicalMatchers.Not(it[0]) }
