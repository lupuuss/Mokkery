package dev.mokkery.internal.tracing

import dev.mokkery.internal.MixingMatchersWithLiteralsException
import dev.mokkery.internal.Mokkery
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.EqMatcher
import kotlin.reflect.KClass

internal interface CallTemplateTracer {

    val templates: List<CallTemplate>

    fun traceArgMatcher(matcher: ArgMatcher)

    fun traceTemplate(mokkery: Mokkery, signature: String, returnType: KClass<*>, args: Array<out Any?>)
}

internal fun CallTemplateTracer(): CallTemplateTracer {
    return CallTemplateTracerImpl()
}

private class CallTemplateTracerImpl : CallTemplateTracer {

    private val matchers = mutableListOf<ArgMatcher>()
    override val templates = mutableListOf<CallTemplate>()


    override fun traceArgMatcher(matcher: ArgMatcher) {
        matchers.add(matcher)
    }

    override fun traceTemplate(mokkery: Mokkery, signature: String, returnType: KClass<*>, args: Array<out Any?>) {
        val matchers = consumeMatchers()
        val registeredMatchers = when {
            args.isEmpty() -> emptyList()
            matchers.isEmpty() -> args.map { EqMatcher(it) }
            matchers.size == args.size -> matchers
            else -> throw MixingMatchersWithLiteralsException(signature)
        }
        templates.add(CallTemplate(mokkery, signature, registeredMatchers))
    }

    private fun consumeMatchers(): List<ArgMatcher> {
        return matchers.toMutableList().also {
            matchers.clear()
        }
    }
}
