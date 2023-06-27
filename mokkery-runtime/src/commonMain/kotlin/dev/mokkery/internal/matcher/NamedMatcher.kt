package dev.mokkery.internal.matcher

import dev.mokkery.matcher.ArgMatcher

internal data class NamedMatcher(
    val name: String,
    val matcher: ArgMatcher<Any?>
) {

    override fun toString(): String = "$name = $matcher"
}
