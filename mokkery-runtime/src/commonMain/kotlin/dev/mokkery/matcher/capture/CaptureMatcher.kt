package dev.mokkery.matcher.capture

import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import dev.mokkery.rendering.argMatcherRenderer

/**
 * Matches an argument with [matcher] and captures arguments into [capture].
 *
 * @see [Capture]
 */
public class CaptureMatcher<T>(
    private val capture: Capture<T>,
    private val matcher: ArgMatcher<T>
) : ArgMatcher.Composite<T>, Renderable {

    override fun matches(arg: T): Boolean = matcher.matches(arg)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CaptureMatcher<*>

        return matcher == other.matcher
    }

    override fun hashCode(): Int = matcher.hashCode()

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "capture($capture, ${scope.argMatcherRenderer.render(matcher)})"

    override fun capture(value: T) {
        capture.capture(value)
        matcher.propagateCapture(value)
    }
}
