@file:Suppress("NOTHING_TO_INLINE", "FunctionName", "UnusedReceiverParameter", "unused")

package dev.mokkery.matcher.logical

import dev.mokkery.annotations.Matcher
import dev.mokkery.internal.utils.generatedCode
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.ArgMatchersScope

/**
 * Matches argument that satisfies all the matchers ([first], [second] and all from [moreMatchers]).
 *
 * Example:
 * ```kotlin
 * // matches every `getForIndex` with arg that is in range 1..4
 * every { dependency.getForIndex(and(gte(1), lte(4))) }
 * ```
 */
public inline fun <T> ArgMatchersScope.and(
    @Matcher first: T,
    @Matcher second: T,
    @Matcher vararg moreMatchers: T
): T = generatedCode

/**
 * Matches argument that satisfies any matcher ([first], [second] or any from [moreMatchers]).

 *
 * ```kotlin
 * // matches every `getForIndex` with arg that is equal 2 or 4
 * every { dependency.getForIndex(or(eq(1), eq(4))) }
 * ```
 */
public inline fun <reified T> ArgMatchersScope.or(
    @Matcher first: T,
    @Matcher second: T,
    @Matcher vararg moreMatchers: T
): T = generatedCode
/**
 * Matches argument that does not satisfy [matcher].
 * It must not receive literals. Only matchers allowed!
 */
public inline fun <reified T> ArgMatchersScope.not(@Matcher matcher: T): T = generatedCode

internal inline fun <T> ArgMatchersScope._andMokkeryMatcher(
    first: ArgMatcher<T>,
    second: ArgMatcher<T>,
    vararg moreMatchers: ArgMatcher<T>
): ArgMatcher.Composite<T> = LogicalMatchers.And(listOf(first, second, *moreMatchers))

internal inline fun <T> ArgMatchersScope._orMokkeryMatcher(
    first: ArgMatcher<T>,
    second: ArgMatcher<T>,
    vararg moreMatchers: ArgMatcher<T>
): ArgMatcher.Composite<T> = LogicalMatchers.Or(listOf(first, second, *moreMatchers))

internal inline fun <T> ArgMatchersScope._notMokkeryMatcher(matcher: ArgMatcher<T>): ArgMatcher.Composite<T> = LogicalMatchers.Not(matcher)

