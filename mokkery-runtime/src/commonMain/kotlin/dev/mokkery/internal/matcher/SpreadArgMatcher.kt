package dev.mokkery.internal.matcher

import dev.drewhamilton.poko.Poko
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.propagateCapture
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import dev.mokkery.rendering.argMatcherRenderer

@PublishedApi
internal interface SpreadArgMatcher<T> : ArgMatcher.Composite<T>

@PublishedApi
internal fun <T> ArgMatcher<T>.spread(): SpreadArgMatcher<T> = SpreadArgMatcherImpl(this)

@Poko
private class SpreadArgMatcherImpl<T>(
    private val matcher: ArgMatcher<T>
) : SpreadArgMatcher<T>, Renderable {

    override fun matches(arg: T): Boolean = matcher.matches(arg)

    override fun capture(value: T) {
        matcher.propagateCapture(value)
    }

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "*${scope.argMatcherRenderer.render(matcher)}"

}
