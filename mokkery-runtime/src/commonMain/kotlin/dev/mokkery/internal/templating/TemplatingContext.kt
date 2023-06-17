package dev.mokkery.internal.templating

import dev.mokkery.internal.MixingMatchersWithLiteralsException
import dev.mokkery.internal.MultipleVarargGenericMatchersException
import dev.mokkery.internal.arrayElementType
import dev.mokkery.internal.arrayToListOrNull
import dev.mokkery.internal.unsafeCast
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.VarArgMatcher

internal interface TemplatingContext {

    val templates: List<CallTemplate>

    fun registerMatcher(matcher: ArgMatcher<Any?>)

    fun saveTemplate(receiver: String, signature: String, varargPosition: Int, args: Array<out Any?>)
}

internal fun TemplatingContext(): TemplatingContext = TemplatingContextImpl()

private class TemplatingContextImpl: TemplatingContext {

    private val matchers = mutableListOf<ArgMatcher<Any?>>()
    override val templates = mutableListOf<CallTemplate>()

    override fun registerMatcher(matcher: ArgMatcher<Any?>) {
        matchers.add(matcher)
    }

    override fun saveTemplate(receiver: String, signature: String, varargPosition: Int, args: Array<out Any?>) {
        val matchers = flush()
        val registeredMatchers = when {
            args.isEmpty() -> emptyList()
            varargPosition != -1 && matchers.isEmpty() -> literalVarargsMatchers(varargPosition, args)
            varargPosition != -1 -> varargMatchers(matchers, varargPosition, args)
            matchers.size == args.size -> matchers
            matchers.isEmpty() -> args.map { ArgMatcher.Equals(it) }
            else -> throw MixingMatchersWithLiteralsException()
        }
        templates += CallTemplate(receiver, signature, registeredMatchers)
    }

    private fun literalVarargsMatchers(
        varargPosition: Int,
        args: Array<out Any?>
    ): List<ArgMatcher<Any?>> {
        val matchers = mutableListOf<ArgMatcher<Any?>>()
        args.forEachIndexed { index, arg ->
            if (varargPosition != index) {
                matchers.add(ArgMatcher.Equals(args[index]))
            } else {
                val varArgMatchers = arg.arrayToListOrNull()
                    .orEmpty()
                    .map { ArgMatcher.Equals(it) }
                val matcher = MergedVarArgMatcher(type = arg.arrayElementType(), before = varArgMatchers)
                matchers.add(matcher.unsafeCast())
            }
        }
        return matchers
    }

    private fun varargMatchers(
        matchers: List<ArgMatcher<Any?>>,
        varargPosition: Int,
        args: Array<out Any?>
    ): List<ArgMatcher<Any?>> {
        val oldMatchers = matchers.toMutableList()
        val newMatchers = mutableListOf<ArgMatcher<Any?>>()
        args.forEachIndexed { index, arg ->
            val currentMatcher = oldMatchers.removeFirstOrNull() ?: throw MixingMatchersWithLiteralsException()
            if (index != varargPosition) {
                newMatchers.add(currentMatcher)
            } else {
                newMatchers.add(buildVarargMatcher(arg, currentMatcher, oldMatchers))
            }
        }
        return newMatchers
    }

    private fun buildVarargMatcher(
        arg: Any?,
        matcher: ArgMatcher<Any?>,
        oldMatchers: MutableList<ArgMatcher<Any?>>,
    ): MergedVarArgMatcher {
        val varArgs = arg.arrayToListOrNull()!!
        val before = mutableListOf<ArgMatcher<Any?>>()
        var wildCardMatcher: VarArgMatcher<Any?>? = null
        val after = mutableListOf<ArgMatcher<Any?>>()
        var currentMatcher = matcher
        varArgs.forEach { _ ->
            when {
                wildCardMatcher != null && currentMatcher is VarArgMatcher<*> -> throw MultipleVarargGenericMatchersException()
                wildCardMatcher != null -> after.add(currentMatcher)
                currentMatcher is VarArgMatcher<*> -> wildCardMatcher = currentMatcher.unsafeCast()
                else -> before.add(currentMatcher)
            }
            currentMatcher = oldMatchers.removeFirstOrNull() ?: throw MixingMatchersWithLiteralsException()
        }
        if (currentMatcher is VarArgMatcher<*>) {
            wildCardMatcher = currentMatcher.unsafeCast()
        } else {
            after.add(currentMatcher)
        }
        return MergedVarArgMatcher(arg.arrayElementType(), before, wildCardMatcher, after)
    }

    private fun flush() = matchers.toMutableList().also { matchers.clear() }

}
