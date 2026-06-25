package dev.mokkery.internal.matcher

import dev.mokkery.internal.rendering.descriptionRenderer
import dev.mokkery.internal.rendering.withGlobalRenderingScope
import dev.mokkery.matcher.ArgMatcher

internal data class MaterializedDefaultValueMatcher(val defaultValue: Any?) : ArgMatcher<Any?> {

    override fun matches(arg: Any?): Boolean = arg == defaultValue

    override fun toString(): String = "default() => ${withGlobalRenderingScope { descriptionRenderer.render(defaultValue) }}"
}
