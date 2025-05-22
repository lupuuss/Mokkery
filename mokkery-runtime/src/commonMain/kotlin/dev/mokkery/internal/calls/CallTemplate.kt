package dev.mokkery.internal.calls

import dev.mokkery.internal.MockId
import dev.mokkery.internal.utils.PropertyDescriptor
import dev.mokkery.matcher.ArgMatcher

internal data class CallTemplate(
    val mockId: MockId,
    val name: String,
    val signature: String,
    val matchers: Map<String, ArgMatcher<Any?>>
) {

    override fun toString(): String = buildString {
        append(mockId.toString())
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
