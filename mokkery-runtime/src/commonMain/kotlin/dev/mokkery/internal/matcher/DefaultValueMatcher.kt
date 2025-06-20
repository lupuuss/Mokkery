package dev.mokkery.internal.matcher

import dev.mokkery.matcher.ArgMatcher

internal data class DefaultValueMatcher<T>(
    val mask: Long,
    val caller: Function<Nothing>,
    val isSuspend: Boolean,
) : ArgMatcher<T> {

    override fun matches(arg: T): Boolean = false

    override fun toString(): String = "default()"
}
