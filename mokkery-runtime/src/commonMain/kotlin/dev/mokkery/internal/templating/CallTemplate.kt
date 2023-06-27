package dev.mokkery.internal.templating

import dev.mokkery.internal.matcher.NamedMatcher

internal data class CallTemplate(
    val receiver: String,
    val name: String,
    val matchers: List<NamedMatcher>
) {

    override fun toString(): String = buildString {
        append(receiver)
        append(".")
        append(name)
        append("(")
        append(matchers.joinToString { "${it.name} = ${it.matcher}" })
        append(")")
    }
}
