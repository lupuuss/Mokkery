package dev.mokkery.matcher

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.annotations.Matcher
import dev.mokkery.internal.utils.mokkeryIntrinsic
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Gt
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Gte
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Lt
import dev.mokkery.matcher.ArgMatcher.Comparing.Type.Lte
import dev.mokkery.matcher.logical.not
import kotlin.reflect.KFunction1

/**
 * Matches argument that satisfies a [matcher].
 * Cannot be used with [ArgMatcher.Composite]. Use [matchesComposite] instead.
*/
@Suppress("UnusedReceiverParameter")
public fun <T> MokkeryMatcherScope.matches(matcher: ArgMatcher<T>): T = mokkeryIntrinsic

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
@Suppress("UnusedReceiverParameter")
@DelicateMokkeryApi
public fun <T : R, R> MokkeryMatcherScope.matchesComposite(
    @Matcher vararg matchers: T,
    builder: (List<ArgMatcher<T>>) -> ArgMatcher.Composite<R>
): R = mokkeryIntrinsic

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
 * **DEPRECATED: This API is considered obsolete. Simply omit this matcher.**
 */
@Deprecated(
    "This API is considered obsolete. Literals can be used in all contexts now, so this matcher usage can be omitted.",
    ReplaceWith("value")
)
public fun <T> MokkeryMatcherScope.eq(value: T): T = matches(ArgMatcher.Equals(value))

/**
 * Matches an argument that is not equal to [value].
 */
@Deprecated(
    "This API is considered obsolete. `not` matcher should be used instead as it's more flexible - allows using matchers.",
    ReplaceWith("not(value)", "dev.mokkery.matcher.logical.not")
)
public fun <T> MokkeryMatcherScope.neq(value: T): T = not(value)

/**
 * **DEPRECATED: Renamed to [ref].**
 */
@Deprecated(
    "Renamed to `ref`",
    ReplaceWith("ref(value)", "dev.mokkery.matcher.ref")
)
public fun <T> MokkeryMatcherScope.eqRef(value: T): T = ref(value)

/**
 * Matches an argument whose reference is not equal to [value]'s reference.
 */
@Deprecated(
    "This API is considered obsolete. Use `not(ref(...)).`",
    ReplaceWith("not(ref(value))", "dev.mokkery.matcher.ref", "dev.mokkery.matcher.logical.not")
)
public fun <T> MokkeryMatcherScope.neqRef(value: T): T = not(ref(value))

/**
 * **DEPRECATED: This API is considered obsolete. Use [matches] instead.**
 */
@Deprecated("This API is considered obsolete. Use `matches` instead.",  ReplaceWith("matches(predicate)", "dev.mokkery.matcher.matches"))
public fun <T> MokkeryMatcherScope.matching(
    predicate: (T) -> Boolean,
): T = matches({ "matching(...)" }, predicate)

/**
 * **DEPRECATED: This API is considered obsolete. Use [matches] instead.**
 */
@Deprecated("This API is considered obsolete. Use `matches` instead.",  ReplaceWith("matches(toString, predicate)", "dev.mokkery.matcher.matches"))
public fun <T> MokkeryMatcherScope.matching(
    toString: () -> String,
    predicate: (T) -> Boolean,
): T = matches(toString, predicate)

/**
 * **DEPRECATED: Renamed to [matchesBy]**
 */
@Deprecated("Renamed to `matchesBy`.", ReplaceWith("matchesBy(function)", "dev.mokkery.matcher.matchesBy"))
public fun <T> MokkeryMatcherScope.matchingBy(
    function: KFunction1<T, Boolean>
): T = matchesBy(function)
