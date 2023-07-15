package dev.mokkery.test

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.ArgMatchersScope
import kotlin.reflect.KClass

internal class TestArgMatchersScope : ArgMatchersScope {

    private val _recordedCalls = mutableListOf<Pair<KClass<*>, ArgMatcher<*>>>()
    private val recordedCalls: List<Pair<KClass<*>, ArgMatcher<*>>> = _recordedCalls

    @Suppress("UNCHECKED_CAST")
    @DelicateMokkeryApi
    override fun <T> matches(argType: KClass<*>, matcher: ArgMatcher<T>): T {
        _recordedCalls.add(argType to matcher)
        return null as T
    }
}
