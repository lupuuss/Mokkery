package dev.mokkery.internal.templating

import dev.mokkery.internal.MixingMatchersWithLiteralsException
import dev.mokkery.matcher.ArgMatcher

internal interface TemplatingContext {

    val templates: List<CallTemplate>

    fun registerMatcher(matcher: ArgMatcher<Any?>)

    fun saveTemplate(receiver: String, signature: String, args: Array<out Any?>)
}

internal fun TemplatingContext(): TemplatingContext = TemplatingContextImpl()

private class TemplatingContextImpl: TemplatingContext {

    private val matchers = mutableListOf<ArgMatcher<Any?>>()
    override val templates = mutableListOf<CallTemplate>()

    override fun registerMatcher(matcher: ArgMatcher<Any?>) {
        matchers.add(matcher)
    }

    override fun saveTemplate(receiver: String, signature: String, args: Array<out Any?>) {
        val matchers = flush()
        val registeredMatchers = when {
            args.isEmpty() -> emptyList()
            matchers.isEmpty() -> args.map { ArgMatcher.Equals(it) }
            matchers.size == args.size -> matchers
            else -> throw MixingMatchersWithLiteralsException(signature)
        }
        templates += CallTemplate(receiver, signature, registeredMatchers)
    }

    private fun flush() = matchers.toMutableList().also { matchers.clear() }

}
