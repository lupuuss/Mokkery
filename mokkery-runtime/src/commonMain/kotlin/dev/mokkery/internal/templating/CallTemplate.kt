package dev.mokkery.internal.templating

import dev.mokkery.matcher.ArgMatcher

internal data class CallTemplate(
    val receiver: String,
    val name: String,
    val signature: String,
    val matchers: Map<String, ArgMatcher<Any?>>
) {

    override fun toString(): String = buildString {
        append(receiver)
        append(".")
        append(name)
        append("(")
        append(matchers.entries.joinToString { "${it.key} = ${it.value}" })
        append(")")
    }
}
