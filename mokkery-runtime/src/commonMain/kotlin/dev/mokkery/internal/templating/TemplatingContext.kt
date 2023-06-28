package dev.mokkery.internal.templating

import dev.mokkery.internal.MultipleMatchersForSingleArgException
import dev.mokkery.internal.MultipleVarargGenericMatchersException
import dev.mokkery.internal.arrayElementType
import dev.mokkery.internal.generateSignature
import dev.mokkery.internal.toListOrNull
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.VarArgMatcher

internal interface TemplatingContext {

    val templates: List<CallTemplate>

    fun registerName(name: String)

    fun registerMatcher(matcher: ArgMatcher<Any?>)

    fun registerVarargElement(arg: Any?)

    fun saveTemplate(receiver: String, name: String, args: List<CallArg>)
}

internal fun TemplatingContext(): TemplatingContext = TemplatingContextImpl()

private class TemplatingContextImpl: TemplatingContext {

    private val currentMatchers = mutableListOf<ArgMatcher<Any?>?>()
    private val matchers = mutableMapOf<String, List<ArgMatcher<Any?>?>>()
    private var varargsMatchersCount = 0
    override val templates = mutableListOf<CallTemplate>()
    override fun registerName(name: String) {
        matchers[name] = currentMatchers.toMutableList()
        currentMatchers.clear()
    }

    override fun registerMatcher(matcher: ArgMatcher<Any?>) {
        if (matcher is VarArgMatcher<*>) varargsMatchersCount++
        currentMatchers.add(matcher)
    }

    override fun registerVarargElement(arg: Any?) {
        val size = arg.toListOrNull()?.size ?: 1
        for (index in 0 until size) {
            val matcher = currentMatchers.getOrNull(varargsMatchersCount + index)
            if (matcher == null) currentMatchers.add(null)
        }
        varargsMatchersCount += size
    }

    override fun saveTemplate(receiver: String, name: String, args: List<CallArg>) {
        val matchers = flush(args)
        templates += CallTemplate(receiver, name, generateSignature(name, args), matchers.toMap())
    }

    private fun flush(args: List<CallArg>): List<Pair<String, ArgMatcher<Any?>>> {
        val matchersSnapshot = matchers.toMutableMap()
        matchers.clear()
        return args.map {
            val matchers = matchersSnapshot[it.name].orEmpty()
            when {
                !it.isVararg && matchers.size > 1 -> throw MultipleMatchersForSingleArgException(it.name, matchers)
                !it.isVararg -> it.name to (matchers.singleOrNull() ?: ArgMatcher.Equals(it.value))
                else -> it.name to varargMatcher(it, matchers)
            }
        }
    }

    private fun varargMatcher(arg: CallArg, matchers: List<ArgMatcher<Any?>?>): ArgMatcher<Any?> {
        val varArgs = arg.value.toListOrNull() ?: error("Unexpected vararg param!")
        val before = mutableListOf<ArgMatcher<Any?>>()
        var wildcardMatcher: VarArgMatcher<Any?>? = null
        val after = mutableListOf<ArgMatcher<Any?>>()
        matchers.forEachIndexed { index, matcher ->
            when {
                wildcardMatcher != null && matcher is VarArgMatcher<Any?> -> throw MultipleVarargGenericMatchersException()
                matcher is VarArgMatcher<Any?> -> wildcardMatcher = matcher
                wildcardMatcher != null -> after.add(matcher ?: ArgMatcher.Equals(varArgs[index - 1]))
                else -> before.add(matcher ?: ArgMatcher.Equals(varArgs[index]))
            }
        }
        return MergedVarArgMatcher(arg.arrayElementType(), before, wildcardMatcher, after)
    }

}
