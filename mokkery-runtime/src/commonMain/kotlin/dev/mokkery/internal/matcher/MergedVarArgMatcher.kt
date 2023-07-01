package dev.mokkery.internal.matcher

import dev.mokkery.internal.toListOrNull
import dev.mokkery.internal.capitalize
import dev.mokkery.internal.varargNameByElementType
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.VarArgMatcher
import kotlin.reflect.KClass

public class MergedVarArgMatcher(
    private val type: KClass<*>,
    private val before: List<ArgMatcher<Any?>> = emptyList(),
    private val wildCard: VarArgMatcher<Any?>? = null,
    private val after: List<ArgMatcher<Any?>> = emptyList()
) : ArgMatcher<Any?> {

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

    override fun toString(): String = buildString {
        when {
            before.isEmpty() && after.isEmpty() && wildCard == null -> append("no${varargNameByElementType(type).capitalize()}()")
            before.isEmpty() && after.isEmpty() -> append(wildCard.toString())
            after.isEmpty() && wildCard == null -> {
                append("[")
                append(before.joinToString())
                append("]")
            }
            else -> {
                append(varargNameByElementType(type))
                append("(")
                val middle = if (wildCard != null) "..." else null
                append((before + listOfNotNull(middle) + after).joinToString())
                append(")")
            }
        }
    }
}
