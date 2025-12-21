package dev.mokkery.internal.matcher

import dev.mokkery.internal.render.Renderers.description
import dev.mokkery.matcher.ArgMatcher

internal data class MaterializedDefaultValueMatcher(val defaultValue: Any?) : ArgMatcher<Any?> {

    override fun matches(arg: Any?): Boolean = arg == defaultValue

    override fun toString(): String = "default() => ${description.render(defaultValue)}"
}
