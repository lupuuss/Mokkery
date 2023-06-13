package dev.mokkery.internal.tracing

import dev.mokkery.internal.Mokkery
import dev.mokkery.matcher.ArgMatcher

internal data class CallTemplate(
    val mokkery: Mokkery,
    val signature: String,
    val matchers: List<ArgMatcher>
) {

    override fun toString(): String = buildString {
        append(mokkery.mockId)
        append(".")
        append(signature.substringBefore("/"))
        append("(")
        append(matchers.joinToString())
        append(")")
    }
}
