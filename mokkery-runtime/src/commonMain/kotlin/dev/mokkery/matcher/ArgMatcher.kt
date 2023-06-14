package dev.mokkery.matcher

import kotlin.reflect.KClass

public sealed interface ArgMatcher {
    public fun match(arg: Any?): Boolean
}

internal data class AnyMatcher(private val cls: KClass<*>) : ArgMatcher {

    override fun match(arg: Any?): Boolean = true

    override fun toString(): String = "any(${cls.simpleName})"
}

internal data class EqMatcher(private val value: Any?) : ArgMatcher {

    override fun match(arg: Any?): Boolean = arg == value

    override fun toString(): String = "eq($value)"
}

internal class CustomMatcher(
    private val cls: KClass<*>,
    private val predicate: (Any?) -> Boolean
) : ArgMatcher {

    override fun match(arg: Any?): Boolean = predicate(arg)

    override fun toString(): String = "matching(${cls.simpleName})"
}
