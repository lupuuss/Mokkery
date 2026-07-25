package dev.mokkery.matcher.nullable

import dev.drewhamilton.poko.Poko
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.annotations.Matcher
import dev.mokkery.internal.rendering.argMatcherRenderer
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.MokkeryMatcherScope
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.propagateCapture
import dev.mokkery.matcher.matchesComposite
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable

/**
 * Matches an argument that is not null and matches [matcher].
 */
public fun <T : Any> MokkeryMatcherScope.notNull(
    @Matcher matcher: T = any()
): T? = matchesComposite(matcher) { NotNullMatcher<T?>(it[0]) }

/**
 * Matches an argument that is not null and matches [matcher].
 */
@DelicateMokkeryApi
@Poko
public class NotNullMatcher<T>(public val matcher: ArgMatcher<T & Any>) : ArgMatcher.Composite<T>, Renderable {

    override fun matches(arg: T): Boolean = arg?.let(matcher::matches) == true

    override fun capture(value: T) {
        if (value != null) {
            matcher.propagateCapture(value)
        }
    }

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "notNull(${scope.argMatcherRenderer.render(matcher)})"
}
