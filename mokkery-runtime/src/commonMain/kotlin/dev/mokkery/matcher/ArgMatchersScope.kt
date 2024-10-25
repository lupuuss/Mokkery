package dev.mokkery.matcher

import dev.mokkery.annotations.DelicateMokkeryApi
import kotlin.reflect.KClass

/**
 * Scope for registering argument matchers.
 */
public interface ArgMatchersScope {

    /**
     * Registers [matcher] with given [argType].
     *
     * @param argType [KClass] of [T]
     */
    @DelicateMokkeryApi
    public fun <T> matches(argType: KClass<*>, matcher: ArgMatcher<T>): T

}

/**
 * Registers [matcher] with [T]::class as argument type.
 */
@DelicateMokkeryApi
public inline fun <reified T> ArgMatchersScope.matches(matcher: ArgMatcher<T>): T = matches(T::class, matcher)
