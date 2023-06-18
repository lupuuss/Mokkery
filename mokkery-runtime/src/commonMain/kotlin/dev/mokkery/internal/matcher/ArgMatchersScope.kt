package dev.mokkery.internal.matcher

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.answering.autofillValue
import dev.mokkery.internal.templating.TemplatingContext
import dev.mokkery.internal.unsafeCast
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.ArgMatchersScope
import kotlin.reflect.KClass

internal fun ArgMatchersScope(context: TemplatingContext): ArgMatchersScope = ArgMatchersScopeImpl(context)

private class ArgMatchersScopeImpl(private val context: TemplatingContext) : ArgMatchersScope {
    @DelicateMokkeryApi
    override fun <T> matches(argType: KClass<*>, matcher: ArgMatcher<T>): T {
        context.registerMatcher(matcher.unsafeCast())
        return autofillValue(argType)
    }

}
