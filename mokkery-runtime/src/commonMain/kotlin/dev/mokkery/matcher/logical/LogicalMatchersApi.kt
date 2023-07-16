package dev.mokkery.matcher.logical

import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.matcher.matches

/**
 * Matches argument that satisfies all the matchers ([first], [second] and all from [moreMatchers]).
 * It must not receive literals. Only matchers allowed!
 *
 * Example:
 * ```kotlin
 * // matches every `getForIndex` with arg that is in range 1..4
 * every { dependency.getForIndex(and(gte(1), lte(4))) }
 * ```
 */
public inline fun <reified T> ArgMatchersScope.and(first: T, second: T, vararg moreMatchers: T): T {
    val matchers = listOf(first, second) + moreMatchers
    return matches(LogicalMatchers.And(matchers.size))
}


/**
 * Matches argument that satisfies any matcher ([first], [second] or any from [moreMatchers]).
 * It must not receive literals. Only matchers allowed!
 *
 * ```kotlin
 * // matches every `getForIndex` with arg that is equal 2 or 4
 * every { dependency.getForIndex(or(eq(1), eq(4))) }
 * ```
 */
public inline fun <reified T> ArgMatchersScope.or(first: T, second: T, vararg moreMatchers: T): T {
    val matchers = listOf(first, second) + moreMatchers
    return matches(LogicalMatchers.And(matchers.size))
}
