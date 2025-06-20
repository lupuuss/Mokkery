package dev.mokkery.internal.templating

import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.utils.PropertyDescriptor
import dev.mokkery.matcher.ArgMatcher

internal data class CallTemplate(
    val instanceId: MokkeryInstanceId,
    val name: String,
    val signature: String,
    val matchers: Map<String, ArgMatcher<Any?>>
) {

    override fun toString(): String = buildString {
        append(instanceId.toString())
        append(".")
        append(toStringNoMockId())
    }

    fun toStringNoMockId(): String = PropertyDescriptor.fromNameOrNull(name)
        ?.toCallString(matchers.values.map(ArgMatcher<Any?>::toString))
        ?: buildString {
            append(name)
            append("(")
            append(matchers.entries.joinToString { "${it.key} = ${it.value}" })
            append(")")
        }
}
