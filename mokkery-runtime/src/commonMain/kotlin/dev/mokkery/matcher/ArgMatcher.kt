package dev.mokkery.matcher

import dev.drewhamilton.poko.Poko
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.utils.bestName
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import dev.mokkery.rendering.descriptionRenderer
import kotlin.reflect.KClass

/**
 * Checks if given argument satisfies provided conditions.
 *
 * A matcher that combines other matchers should implement [ArgMatcher.Composite].
 */
public fun interface ArgMatcher<in T> {

    public fun matches(arg: T): Boolean

    /**
     * Matches any argument.
     */
    public data object Any : ArgMatcher<kotlin.Any?>, Renderable {

        override fun matches(arg: kotlin.Any?): Boolean = true

        context(scope: MokkeryRenderingScope)
        override fun render(): String = "any()"
    }

    /**
     * Matches an argument that is equal to [value].
     */
    @Poko
    public class Equals<T>(public val value: T) : ArgMatcher<T>, Renderable {

        override fun matches(arg: T): Boolean = arg == value

        context(scope: MokkeryRenderingScope)
        override fun render(): String = scope.descriptionRenderer.render(value)
    }

    /**
     * Matches an argument whose reference is equal to [value]'s reference.
     */
    public class EqualsRef<T>(public val value: T) : ArgMatcher<T>, Renderable {

        override fun matches(arg: T): Boolean = arg === value

        context(scope: MokkeryRenderingScope)
        override fun render(): String = "ref(${scope.descriptionRenderer.render(value)})"
    }

    /**
     * Matches any [Comparable] [value] depending on [type] parameter.
     */
    @Poko
    public class Comparing<T>(
        public val value: T,
        public val type: Type,
    ) : ArgMatcher<T>, Renderable where T : Comparable<T> {
        override fun matches(arg: T): Boolean = type.compare(arg.compareTo(value))

        context(scope: MokkeryRenderingScope)
        override fun render(): String = "${type.toString().lowercase()}(${scope.descriptionRenderer.render(value)})"

        public enum class Type(public val compare: (Int) -> Boolean) {
            Eq({ it == 0 }), Lt({ it < 0 }), Lte({ it <= 0 }), Gt({ it > 0 }), Gte({ it >= 0 })
        }
    }

    /**
     * Matches an argument that is instance of [type].
     */
    @Poko
    public class OfType<T>(public val type: KClass<*>) : ArgMatcher<T>, Renderable {

        override fun matches(arg: T): Boolean = type.isInstance(arg)

        context(scope: MokkeryRenderingScope)
        override fun render(): String = "ofType<${type.bestName()}>()"
    }

    /**
     * A matcher that combines other matchers (e.g. [dev.mokkery.matcher.logical.LogicalMatchers.Or]).
     *
     * An implementation accepts its child matchers in any form it needs (e.g. `List<ArgMatcher<T>>`)
     * and **must** propagate [Capture.capture] to its direct children:
     * ```kotlin
     * public class Or<T>(
     *     public val matchers: List<ArgMatcher<T>>
     * ) : ArgMatcher.Composite<T> {
     *     override fun matches(arg: T): Boolean = matchers.any { it.matches(arg) }
     *
     *     override fun capture(value: T) {
     *         matchers.propagateCapture(value)
     *     }
     * }
     * ```
     *
     * To use it, write a [MokkeryMatcherScope] extension like for a regular matcher, mark the parameters that
     * accept matchers with [dev.mokkery.annotations.Matcher] and turn them into matcher objects with
     * [dev.mokkery.matcher.materialize]:
     * ```kotlin
     * fun <T> MokkeryMatcherScope.or(@Matcher first: T, @Matcher second: T, @Matcher vararg others: T): T {
     *     return matches(Or(materialize(first, second, *others)))
     * }
     * ```
     *
     * Existing implementations:
     * * [dev.mokkery.matcher.capture.capture]
     * * [dev.mokkery.matcher.logical.or]
     * * [dev.mokkery.matcher.logical.not]
     * * [dev.mokkery.matcher.logical.and]
     * * [dev.mokkery.matcher.nullable.notNull]
     */
    @DelicateMokkeryApi
    public interface Composite<T> : ArgMatcher<T>, Capture<T> {

        /**
         * Propagates [value] to children matchers.
         */
        override fun capture(value: T)
    }
}
