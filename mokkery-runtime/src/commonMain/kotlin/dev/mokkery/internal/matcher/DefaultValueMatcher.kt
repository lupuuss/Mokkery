package dev.mokkery.internal.matcher

import dev.mokkery.matcher.ArgMatcher

internal data class DefaultValueMatcher(
    val mask: Long,
    val caller: Function<Nothing>,
    val isSuspend: Boolean,
) : ArgMatcher<Any?> {

    override fun matches(arg: Any?): Boolean = false

    override fun toString(): String = "default()"
}
