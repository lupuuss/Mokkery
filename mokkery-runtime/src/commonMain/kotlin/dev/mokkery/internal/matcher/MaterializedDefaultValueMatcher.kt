package dev.mokkery.internal.matcher

import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import dev.mokkery.rendering.descriptionRenderer

internal data class MaterializedDefaultValueMatcher(val defaultValue: Any?) : ArgMatcher<Any?>, Renderable {

    override fun matches(arg: Any?): Boolean = arg == defaultValue

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "default() => ${scope.descriptionRenderer.render(defaultValue)}"
}
