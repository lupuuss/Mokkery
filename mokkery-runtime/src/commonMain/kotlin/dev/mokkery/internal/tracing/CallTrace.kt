package dev.mokkery.internal.tracing

import dev.mokkery.context.CallArgument
import dev.mokkery.internal.callFunctionToString
import dev.mokkery.internal.callToString

internal data class CallTrace(
    val receiver: String,
    val name: String,
    val args: List<CallArgument>,
    val orderStamp: Long,
) {
    override fun toString(): String = callToString(receiver, name, args)

    fun toStringNoReceiver() = callFunctionToString(name, args)
}

