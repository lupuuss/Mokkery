package dev.mokkery.matcher

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.annotations.Matcher
import dev.mokkery.internal.utils.toBeReplacedByCompilerPlugin
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Gt
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Gte
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Lt
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Lte
import kotlin.reflect.KFunction1

/**
 * Matches argument that satisfies a [matcher].
 * Cannot be used with [ArgMatcher.Composite]. Use [matchesComposite] instead.
*/
@Suppress("UnusedReceiverParameter", "NOTHING_TO_INLINE")
public inline fun <T> MokkeryMatcherScope.matches(matcher: ArgMatcher<T>): T = toBeReplacedByCompilerPlugin

/**
 * Matches an argument that satisfies a matcher composed of [matchers] with [builder].
 *
 * Matchers used to create a composite must be passed via the [matchers] vararg.
 * The [builder] receives a list of actual matcher objects, in the same order as provided in the vararg,
 * which can then be composed.
 * Any other arguments must be passed directly to the matcher constructor inside the [builder].
 *
 * It is allowed to use literal values as matchers; they are automatically wrapped with
 * [dev.mokkery.matcher.ArgMatcher.Equals].
 *
 * Samples:
 * * [dev.mokkery.matcher.capture.capture]
 * * [dev.mokkery.matcher.logical.or]
 * * [dev.mokkery.matcher.logical.not]
 * * [dev.mokkery.matcher.logical.and]
 * * [dev.mokkery.matcher.nullable.notNull]
 */
@Suppress("UnusedReceiverParameter", "NOTHING_TO_INLINE")
@DelicateMokkeryApi
public inline fun <T : R, R> MokkeryMatcherScope.matchesComposite(
    @Matcher vararg matchers: T,
    builder: (List<ArgMatcher<T>>) -> ArgMatcher.Composite<R>
): R = toBeReplacedByCompilerPlugin

/**
 * Matches any argument.
 */
public fun <T> MokkeryMatcherScope.any(): T = matches(ArgMatcher.Any)

/**
 * Matches an argument that is equal to [value]. It can be replaced with [value] literal.
 */
public fun <T> MokkeryMatcherScope.eq(value: T): T = matches(ArgMatcher.Equals(value))

/**
 * Matches an argument that is not equal to [value].
 */
public fun <T> MokkeryMatcherScope.neq(value: T): T = matches(ArgMatcher.NotEqual(value))

/**
 * Matches an argument whose reference is equal to [value]'s reference.
 */
public fun <T> MokkeryMatcherScope.eqRef(value: T): T = matches(ArgMatcher.EqualsRef(value))

/**
 * Matches an argument whose reference is not equal to [value]'s reference.
 */
public fun <T> MokkeryMatcherScope.neqRef(value: T): T = matches(ArgMatcher.NotEqualRef(value))

/**
 * Matches an argument according to the [predicate]. Registered matcher [Any.toString] calls [toString].
 */
public fun <T> MokkeryMatcherScope.matching(
    toString: () -> String = { "matching(...)" },
    predicate: (T) -> Boolean,
): T = matches(ArgMatcher.Matching(predicate, toString))

/**
 * Matches an argument by calling given [function]. Also, it returns function name on [Any.toString].
 */
public fun <T> MokkeryMatcherScope.matchingBy(
    function: KFunction1<T, Boolean>
): T = matches(ArgMatcher.Matching(function) { "${function.name}()" })

/**
 * Matches argument that is less than [value].
 */
public fun <T : Comparable<T>> MokkeryMatcherScope.lt(
    value: T
): T = matches(ArgMatcher.Comparing(value, Lt))

/**
 * Matches an argument that is less than or equal to [value].
 */
public fun <T : Comparable<T>> MokkeryMatcherScope.lte(
    value: T
): T = matches(ArgMatcher.Comparing(value, Lte))

/**
 * Matches argument that is greater than [value].
 */
public fun <T : Comparable<T>> MokkeryMatcherScope.gt(
    value: T
): T = matches(ArgMatcher.Comparing(value, Gt))

/**
 * Matches an argument that is greater than or equal to [value].
 */
public fun <T : Comparable<T>> MokkeryMatcherScope.gte(
    value: T
): T = matches(ArgMatcher.Comparing(value, Gte))

/**
 * Matches an argument that is an instance of type [T].
 */
public inline fun <reified T> MokkeryMatcherScope.ofType(): T = matches(ArgMatcher.OfType(T::class))
