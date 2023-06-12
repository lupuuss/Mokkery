package dev.mokkery.matcher

import dev.mokkery.MixingMatchersWithLiteralsException
import dev.mokkery.Mokkery
import dev.mokkery.tracking.CallTemplate
import kotlin.reflect.KClass

internal interface CallTemplateRegistry {

    val templates: List<CallTemplate>

    fun registerArgMatcher(matcher: ArgMatcher)

    fun saveTemplate(mokkery: Mokkery, signature: String, returnType: KClass<*>, args: Array<out Any?>)
}

internal fun CallTemplateRegistry(): CallTemplateRegistry {
    return CallTemplateRegistryImpl()
}

private class CallTemplateRegistryImpl : CallTemplateRegistry {

    private val matchers = mutableListOf<ArgMatcher>()
    override val templates = mutableListOf<CallTemplate>()


    override fun registerArgMatcher(matcher: ArgMatcher) {
        matchers.add(matcher)
    }

    override fun saveTemplate(mokkery: Mokkery, signature: String, returnType: KClass<*>, args: Array<out Any?>) {
        val matchers = consume()
        val registeredMatchers = when {
            args.isEmpty() -> emptyList()
            matchers.isEmpty() -> args.map { EqMatcher(it) }
            matchers.size == args.size -> matchers
            else -> throw MixingMatchersWithLiteralsException(signature)
        }
        templates.add(CallTemplate(mokkery, signature, registeredMatchers))
    }

    private fun consume(): List<ArgMatcher> {
        return matchers.toMutableList().also {
            matchers.clear()
        }
    }
}
