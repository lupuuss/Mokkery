package dev.mokkery.internal.matcher

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.answering.autofillValue
import dev.mokkery.internal.templating.TemplatingScope
import dev.mokkery.internal.unsafeCast
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.ArgMatchersScope
import kotlin.reflect.KClass

internal fun ArgMatchersScope(scope: TemplatingScope): ArgMatchersScope = ArgMatchersScopeImpl(scope)

private class ArgMatchersScopeImpl(private val scope: TemplatingScope) : ArgMatchersScope {
    @DelicateMokkeryApi
    override fun <T> matches(argType: KClass<*>, matcher: ArgMatcher<T>): T {
        scope.registerMatcher(matcher.unsafeCast())
        return autofillValue(argType)
    }

}
