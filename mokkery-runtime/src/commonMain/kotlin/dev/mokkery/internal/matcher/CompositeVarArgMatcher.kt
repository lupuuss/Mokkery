package dev.mokkery.internal.matcher

import dev.mokkery.internal.MultipleVarargGenericMatchersException
import dev.mokkery.internal.toListOrNull
import dev.mokkery.internal.toPlatformArrayOf
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.propagateCapture
import dev.mokkery.matcher.varargs.VarArgMatcher
import kotlin.reflect.KClass

internal data class CompositeVarArgMatcher(
    val type: KClass<*>,
    val matchers: List<ArgMatcher<Any?>> = emptyList()
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
        val arrayAsList = arg.toListOrNull() ?: return false
        if (before.size + after.size > arrayAsList.size) return false
        val beforePart = arrayAsList.subList(0, before.size)
        if (before.zip(beforePart).any { (matcher, arg) -> !matcher.matches(arg) }) return false
        val afterPart = arrayAsList.subList(arrayAsList.size - after.size, arrayAsList.size)
        if (after.zip(afterPart).any { (matcher, arg) -> !matcher.matches(arg) }) return false
        val rest = arrayAsList.subList(before.size, arrayAsList.size - after.size)
        return wildCard?.matches(rest) ?: rest.isEmpty()
    }

    override fun compose(matcher: ArgMatcher<Any?>): ArgMatcher.Composite<Any?> {
        return when {
            wildCard != null && matcher is VarArgMatcher -> throw MultipleVarargGenericMatchersException()
            else -> copy(matchers = listOf(matcher) + matchers)
        }
    }

    override fun isFilled(): Boolean = false

    override fun assertFilled() = Unit

    override fun capture(value: Any?) {
        val arrayAsList = value.toListOrNull() ?: return
        if (before.size + after.size > arrayAsList.size) return
        val beforePart = arrayAsList.subList(0, before.size)
        before.zip(beforePart).forEach { (matcher, arg) ->
            matcher.propagateCapture(arg)
        }
        val afterPart = arrayAsList.subList(arrayAsList.size - after.size, arrayAsList.size)
        after.zip(afterPart).forEach { (matcher, arg) ->
            matcher.propagateCapture(arg)
        }
        val rest = arrayAsList.subList(before.size, arrayAsList.size - after.size)
        wildCard?.propagateCapture(rest.toPlatformArrayOf(value))
    }

    override fun toString(): String = "[${matchers.joinToString()}]"
}
