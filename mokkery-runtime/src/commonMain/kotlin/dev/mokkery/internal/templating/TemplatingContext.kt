@file:Suppress("UNCHECKED_CAST")

package dev.mokkery.internal.templating

import dev.mokkery.internal.UnprocessableMatchersSequenceException
import dev.mokkery.internal.arrayElementType
import dev.mokkery.internal.arrayToListOrNull
import dev.mokkery.internal.unsafeCast
import dev.mokkery.matcher.ArgMatcher

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
            else -> throw UnprocessableMatchersSequenceException()
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
            var currentMatcher = oldMatchers.removeFirstOrNull() ?: ArgMatcher.Equals(arg)
            if (index != varargPosition) newMatchers.add(currentMatcher)
            else {
                val varArgs = arg.arrayToListOrNull() ?: throw UnprocessableMatchersSequenceException()
                val before = mutableListOf<ArgMatcher<Any?>>()
                var wildCardMatcher: ArgMatcher.VarArg<Any?>? = null
                val after = mutableListOf<ArgMatcher<Any?>>()
                varArgs.forEach { vArg ->
                    when {
                        wildCardMatcher != null && currentMatcher is ArgMatcher.VarArg<*> -> throw UnprocessableMatchersSequenceException()
                        wildCardMatcher != null -> after.add(currentMatcher)
                        currentMatcher is ArgMatcher.VarArg<*> -> wildCardMatcher = currentMatcher.unsafeCast()
                        else ->  before.add(currentMatcher)
                    }
                    currentMatcher = oldMatchers.removeFirstOrNull() ?: ArgMatcher.Equals(vArg)
                }
                if (currentMatcher is ArgMatcher.VarArg<*>) {
                    wildCardMatcher = currentMatcher.unsafeCast()
                } else {
                    after.add(currentMatcher)
                }
                val matcher = MergedVarArgMatcher(
                    type = arg.arrayElementType(),
                    before = before,
                    wildCard = wildCardMatcher,
                    after = after
                )
                newMatchers.add(matcher)
            }
        }
        return newMatchers
    }

    private fun flush() = matchers.toMutableList().also { matchers.clear() }

}
