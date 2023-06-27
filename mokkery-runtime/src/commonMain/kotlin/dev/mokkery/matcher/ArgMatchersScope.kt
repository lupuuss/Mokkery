package dev.mokkery.matcher

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.annotations.InternalMokkeryApi
import kotlin.reflect.KClass

public interface ArgMatchersScope {

    @DelicateMokkeryApi
    public fun <T> matches(argType: KClass<*>, matcher: ArgMatcher<T>): T

    @InternalMokkeryApi
    public fun <T> named(name: String, arg: T): T
}

@DelicateMokkeryApi
public inline fun <reified T> ArgMatchersScope.matches(matcher: ArgMatcher<T>): T = matches(T::class, matcher)
