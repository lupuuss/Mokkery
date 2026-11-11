package dev.mokkery.internal.matcher

import dev.drewhamilton.poko.Poko
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.propagateCapture
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import dev.mokkery.rendering.argMatcherRenderer

@Poko
internal class SpreadArgMatcher<T>(val matcher: ArgMatcher<Any?>) : ArgMatcher.Composite<T>, Renderable {

    override fun matches(arg: T): Boolean = matcher.matches(arg)

    override fun capture(value: T) {
        matcher.propagateCapture(value)
    }

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "*${scope.argMatcherRenderer.render(matcher)}"
}
