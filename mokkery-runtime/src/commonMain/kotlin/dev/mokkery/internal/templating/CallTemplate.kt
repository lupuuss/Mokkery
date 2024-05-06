package dev.mokkery.internal.templating

import dev.mokkery.internal.PropertyDescriptor
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
        append(toStringNoReceiver())
    }

    fun toStringNoReceiver(): String = PropertyDescriptor.fromNameOrNull(name)
        ?.toCallString(matchers.values.map(ArgMatcher<Any?>::toString))
        ?: buildString {
            append(name)
            append("(")
            append(matchers.entries.joinToString { "${it.key} = ${it.value}" })
            append(")")
        }
}
