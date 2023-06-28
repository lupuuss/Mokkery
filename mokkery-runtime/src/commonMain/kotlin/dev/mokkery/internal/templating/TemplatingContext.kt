package dev.mokkery.internal.templating

import dev.mokkery.internal.MultipleMatchersForSingleArgException
import dev.mokkery.internal.MultipleVarargGenericMatchersException
import dev.mokkery.internal.arrayElementType
import dev.mokkery.internal.matcher.NamedMatcher
import dev.mokkery.internal.toListOrNull
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.VarArgMatcher

internal interface TemplatingContext {

    val templates: List<CallTemplate>

    fun registerName(name: String)

    fun registerMatcher(matcher: ArgMatcher<Any?>)

    fun saveTemplate(receiver: String, name: String, args: Array<out CallArg>)
}

internal fun TemplatingContext(): TemplatingContext = TemplatingContextImpl()

private class TemplatingContextImpl: TemplatingContext {

    private val currentMatchers = mutableListOf<ArgMatcher<Any?>>()
    private val matchers = mutableMapOf<String, List<ArgMatcher<Any?>>>()
    override val templates = mutableListOf<CallTemplate>()
    override fun registerName(name: String) {
        matchers[name] = currentMatchers.toMutableList()
        currentMatchers.clear()
    }

    override fun registerMatcher(matcher: ArgMatcher<Any?>) {
        currentMatchers.add(matcher)
    }

    override fun saveTemplate(receiver: String, name: String, args: Array<out CallArg>) {
        val matchers = flush(args)
        templates += CallTemplate(receiver, name, matchers)
    }

    private fun flush(args: Array<out CallArg>): List<NamedMatcher> {
        val matchersSnapshot = matchers.toMutableMap()
        matchers.clear()
        return args.map {
            val matchers = matchersSnapshot[it.name].orEmpty()
            when {
                !it.isVararg && matchers.size > 1 -> throw MultipleMatchersForSingleArgException(it.name, matchers)
                !it.isVararg -> NamedMatcher(it.name, matchers.singleOrNull() ?: ArgMatcher.Equals(it.value))
                else -> NamedMatcher(it.name, varargMatcher(it, matchers))
            }
        }
    }

    private fun varargMatcher(arg: CallArg, matchers: List<ArgMatcher<Any?>>): ArgMatcher<Any?> {
        val varArgs = arg.value.toListOrNull() ?: error("Unexpected vararg param!")
        val before = mutableListOf<ArgMatcher<Any?>>()
        var wildcardMatcher: VarArgMatcher<Any?>? = null
        val after = mutableListOf<ArgMatcher<Any?>>()
        varArgs.forEachIndexed { index, varArg ->
            val matcher = matchers.getOrNull(index)
            when {
                wildcardMatcher != null && matcher is VarArgMatcher<Any?> -> throw MultipleVarargGenericMatchersException()
                matcher is VarArgMatcher<Any?> -> wildcardMatcher = matcher
                wildcardMatcher != null -> after.add(matcher ?: ArgMatcher.Equals(varArg))
                else -> before.add(matcher ?: ArgMatcher.Equals(varArg))
            }
        }
        return MergedVarArgMatcher(arg.arrayElementType(), before, wildcardMatcher, after)
    }

}
