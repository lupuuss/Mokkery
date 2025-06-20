package dev.mokkery.internal.matcher

import dev.mokkery.internal.utils.asListOrNull
import dev.mokkery.internal.utils.toPlatformArrayOf
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.propagateCapture
import dev.mokkery.matcher.varargs.VarArgMatcher

internal data class CompositeVarArgMatcher(
    val matchers: List<ArgMatcher<Any?>>
) : ArgMatcher.Composite<Any?> {

    private val wildCard: VarArgMatcher? = matchers.filterIsInstance<VarArgMatcher>().firstOrNull()
    private val before: List<ArgMatcher<Any?>> = if (wildCard != null) {
        matchers.subList(0, matchers.indexOf(wildCard))
    } else {
        matchers
    }
    private val after: List<ArgMatcher<Any?>> = if (wildCard != null) {
        matchers.subList(matchers.indexOf(wildCard) + 1, matchers.size)
    } else {
        emptyList()
    }

    override fun matches(arg: Any?): Boolean {
        val elements = arg.asListOrNull() ?: return false
        if (before.size + after.size > elements.size) return false
        val beforePart = elements.subList(0, before.size)
        if (before.zip(beforePart).any { (matcher, arg) -> !matcher.matches(arg) }) return false
        val afterPart = elements.subList(elements.size - after.size, elements.size)
        if (after.zip(afterPart).any { (matcher, arg) -> !matcher.matches(arg) }) return false
        val rest = elements.subList(before.size, elements.size - after.size)
        return wildCard?.matches(rest) ?: rest.isEmpty()
    }

    override fun capture(value: Any?) {
        val elements = value.asListOrNull() ?: return
        if (before.size + after.size > elements.size) return
        val beforePart = elements.subList(0, before.size)
        before.zip(beforePart).forEach { (matcher, arg) ->
            matcher.propagateCapture(arg)
        }
        val afterPart = elements.subList(elements.size - after.size, elements.size)
        after.zip(afterPart).forEach { (matcher, arg) ->
            matcher.propagateCapture(arg)
        }
        val rest = elements.subList(before.size, elements.size - after.size)
        wildCard?.propagateCapture(rest.toPlatformArrayOf(value))
    }

    override fun toString(): String = "[${matchers.joinToString()}]"
}
