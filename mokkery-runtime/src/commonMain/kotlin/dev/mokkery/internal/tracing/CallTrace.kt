package dev.mokkery.internal.tracing

import dev.mokkery.context.CallArgument
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.utils.callFunctionToString
import dev.mokkery.internal.utils.callToString

internal data class CallTrace(
    val instanceId: MokkeryInstanceId,
    val name: String,
    val args: List<CallArgument>,
    val orderStamp: Long,
) {
    override fun toString(): String = callToString(instanceId, name, args)

    fun toStringNoMockId() = callFunctionToString(name, args)
}

