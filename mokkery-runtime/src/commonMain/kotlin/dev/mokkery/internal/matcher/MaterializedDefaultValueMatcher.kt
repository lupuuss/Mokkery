package dev.mokkery.internal.matcher

import dev.mokkery.MokkeryScope
import dev.mokkery.internal.context.tools
import dev.mokkery.matcher.ArgMatcher

internal data class MaterializedDefaultValueMatcher(val defaultValue: Any?) : ArgMatcher<Any?> {

    override fun matches(arg: Any?): Boolean = arg == defaultValue

    override fun toString(): String = "default() => ${MokkeryScope.global.tools.renderers.description.render(defaultValue)}"
}
