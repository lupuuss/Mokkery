package dev.mokkery.matcher

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.annotations.Matcher
import dev.mokkery.internal.mokkeryIntrinsic
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Gt
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Gte
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Lt
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Lte
import kotlin.reflect.KFunction1

/**
 * Matches argument that satisfies a [matcher].
 */
@Suppress("UnusedReceiverParameter")
public fun <T> MokkeryMatcherScope.matches(matcher: ArgMatcher<T>): T = mokkeryIntrinsic

/**
 * Returns the [ArgMatcher] object behind [matcher]. A literal value is returned as [ArgMatcher.Equals].
 *
 * It is the inverse of [matches]: `materialize(matches(matcher))` returns `matcher`.
 */
@Suppress("UnusedReceiverParameter")
public fun <T> MokkeryMatcherScope.materialize(@Matcher matcher: T): ArgMatcher<T> = mokkeryIntrinsic

/**
 * Like single-value [materialize], but for many [matchers] at once, including vararg parameters marked with [Matcher].
 * Returns their [ArgMatcher] objects in order.
 *
 * Matchers and literal values can be mixed. A vararg parameter **must** be passed with the spread operator:
 * ```kotlin
 * fun <T> MokkeryMatcherScope.oneOf(@Matcher first: T, @Matcher vararg others: T): T {
 *     return matches(LogicalMatchers.Or(materialize(first, *others)))
 * }
 * ```
 */
@Suppress("UnusedReceiverParameter")
public fun <T> MokkeryMatcherScope.materialize(@Matcher vararg matchers: T): List<ArgMatcher<T>> = mokkeryIntrinsic

/**
 * Matches an argument according to the [predicate]. Registered matcher [Any.toString] calls [toString].
 */
public fun <T> MokkeryMatcherScope.matches(
    toString: () -> String,
    predicate: (T) -> Boolean,
): T = matches(
    object : ArgMatcher<T> {
        override fun matches(arg: T): Boolean = predicate(arg)

        override fun toString(): String = toString()
    }
)

/**
 * Matches an argument by calling given [function]. Also, it returns function name on [Any.toString].
 */
public fun <T> MokkeryMatcherScope.matchesBy(function: KFunction1<T, Boolean>): T = matches(
    toString = { "${function.name}()" },
    predicate = function
)

/**
 * Matches any argument.
 */
public fun <T> MokkeryMatcherScope.any(): T = matches(ArgMatcher.Any)

/**
 * Matches an argument whose reference is equal to [value]'s reference.
 */
public fun <T> MokkeryMatcherScope.ref(value: T): T = matches(ArgMatcher.EqualsRef(value))

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

/**
 * **DEPRECATED**
 * Matches an argument that satisfies a composite matcher created by [builder] from [matchers].
 *
 * To migrate, get the matcher objects with [materialize] and pass the composite to [matches]:
 * ```kotlin
 * // before
 * fun <T> MokkeryMatcherScope.or(@Matcher first: T, @Matcher second: T): T {
 *     return matchesComposite(first, second) { LogicalMatchers.Or(it) }
 * }
 *
 * // after
 * fun <T> MokkeryMatcherScope.or(@Matcher first: T, @Matcher second: T): T {
 *     return matches(LogicalMatchers.Or(materialize(first, second)))
 * }
 * ```
 */
@Deprecated(message = "Replaced by `matches` combined with `materialize`. See the migration guide in the `matchesComposite` KDoc.")
@DelicateMokkeryApi
public fun <T : R, R> MokkeryMatcherScope.matchesComposite(
    @Matcher vararg matchers: T,
    builder: (List<ArgMatcher<T>>) -> ArgMatcher.Composite<R>
): R = matches(builder(materialize(matchers = matchers)))
