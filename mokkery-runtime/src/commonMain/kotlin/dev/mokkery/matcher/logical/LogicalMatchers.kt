package dev.mokkery.matcher.logical

import dev.drewhamilton.poko.Poko
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.rendering.argMatcherRenderer
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.propagateCapture
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable

/**
 * Contains composite matchers for logical operations.
 */
public object LogicalMatchers {

    /**
     * Matches argument that satisfies all the [matchers].
     */
    @DelicateMokkeryApi
    @Poko
    public class And<T>(public val matchers: List<ArgMatcher<T>>): ArgMatcher.Composite<T>, Renderable {
        override fun matches(arg: T): Boolean = matchers.all { it.matches(arg) }

        context(scope: MokkeryRenderingScope)
        override fun render(): String = "and(${matchers.joinToString { argMatcherRenderer.render(it) }})"

        override fun capture(value: T) {
            matchers.propagateCapture(value)
        }
    }

    /**
     * Matches argument that satisfies any matcher from [matchers].
     */
    @DelicateMokkeryApi
    @Poko
    public class Or<T>(
        public val matchers: List<ArgMatcher<T>>
    ): ArgMatcher.Composite<T>, Renderable {
        override fun matches(arg: T): Boolean = matchers.any { it.matches(arg) }

        context(scope: MokkeryRenderingScope)
        override fun render(): String = "or(${matchers.joinToString { argMatcherRenderer.render(it) }})"

        override fun capture(value: T) {
            matchers.propagateCapture(value)
        }
    }

    /**
     * Matches argument that does not satisfy [matcher].
     */
    @DelicateMokkeryApi
    @Poko
    public class Not<T>(public val matchers: List<ArgMatcher<T>>) : ArgMatcher.Composite<T>, Renderable {

        @Deprecated(
            "This field should not be used anymore. Now, `Not` matcher might contain more than one matcher.",
            ReplaceWith("matchers[0]"),
            DeprecationLevel.ERROR
        )
        public val matcher: ArgMatcher<T> get() = matchers[0]

        @Deprecated(
            "This constructor should not be used anymore. Now, `Not` matcher might contain more than one matcher.",
            ReplaceWith("Not(listOf<_>(matcher))"),
            DeprecationLevel.ERROR
        )
        public constructor(matcher: ArgMatcher<T>) : this(listOf(matcher))

        override fun matches(arg: T): Boolean = matchers.none { it.matches(arg) }

        context(scope: MokkeryRenderingScope)
        override fun render(): String = "not(${matchers.joinToString { argMatcherRenderer.render(it) }})"

        override fun capture(value: T) {
            matchers.propagateCapture(value)
        }
    }
}
