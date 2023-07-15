package dev.mokkery.internal.tracing

import dev.mokkery.internal.callToString

internal data class CallTrace(
    val receiver: String,
    val name: String,
    val args: List<CallArg>,
    val orderStamp: Long,
) {
    override fun toString(): String = callToString(receiver, name, args)
}

