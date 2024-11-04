package dev.mokkery.internal.calls

import dev.mokkery.context.CallArgument
import dev.mokkery.internal.utils.callFunctionToString
import dev.mokkery.internal.utils.callToString

internal data class CallTrace(
    val receiver: String,
    val name: String,
    val args: List<CallArgument>,
    val orderStamp: Long,
) {
    override fun toString(): String = callToString(receiver, name, args)

    fun toStringNoReceiver() = callFunctionToString(name, args)
}

