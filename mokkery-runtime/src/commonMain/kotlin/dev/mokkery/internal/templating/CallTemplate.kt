package dev.mokkery.internal.templating

import dev.mokkery.matcher.ArgMatcher

internal data class CallTemplate(
    val receiver: String,
    val signature: String,
    val matchers: List<ArgMatcher<Any?>>
) {

    override fun toString(): String = buildString {
        append(receiver)
        append(".")
        append(signature.substringBefore("/"))
        append("(")
        append(matchers.joinToString())
        append(")")
    }
}
