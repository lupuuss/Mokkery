package dev.mokkery.internal.templating

import dev.mokkery.internal.matcher.NamedMatcher
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.matcher.ArgMatcher

internal interface TemplatingContext {

    val templates: List<CallTemplate>

    fun registerName(name: String)

    fun registerMatcher(matcher: ArgMatcher<Any?>)

    fun saveTemplate(receiver: String, name: String, args: Array<out CallArg>)
}

internal fun TemplatingContext(): TemplatingContext = TemplatingContextImpl()

private class TemplatingContextImpl: TemplatingContext {

    private var currentMatcher: ArgMatcher<Any?>? = null
    private val names = mutableListOf<String>()
    private val matchers = mutableListOf<ArgMatcher<Any?>?>()
    override val templates = mutableListOf<CallTemplate>()
    override fun registerName(name: String) {
        names.add(name)
        matchers.add(currentMatcher)
    }

    override fun registerMatcher(matcher: ArgMatcher<Any?>) {
        currentMatcher = matcher
    }

    override fun saveTemplate(receiver: String, name: String, args: Array<out CallArg>) {
        val matchers = flush(args)
        templates += CallTemplate(receiver, name, matchers)
    }

    private fun flush(args: Array<out CallArg>) = names.zip(matchers) { name, matcher ->
        NamedMatcher(name, matcher ?: ArgMatcher.Equals(args.first { it.name == name }.value))
    }

}
