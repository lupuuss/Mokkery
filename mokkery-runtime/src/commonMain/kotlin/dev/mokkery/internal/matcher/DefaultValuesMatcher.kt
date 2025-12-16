package dev.mokkery.internal.matcher

import dev.mokkery.matcher.ArgMatcher

internal data class DefaultValuesMatcher(
    val mask: Long,
    val extractingFunction: Function<Nothing>,
    val isExtractingFunctionSuspend: Boolean,
) : ArgMatcher<Any?> {

    override fun matches(arg: Any?): Boolean = false

    override fun toString(): String = "default()"
}
