package dev.mokkery.internal.tracing

import dev.mokkery.MokkeryCallScope
import dev.mokkery.call
import dev.mokkery.context.CallArgument
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.context.instanceSpec

internal data class CallTrace(
    val instanceId: MokkeryInstanceId,
    val name: String,
    val args: List<CallArgument>,
    val orderStamp: Long,
): Comparable<CallTrace> {

    override fun compareTo(other: CallTrace) = this.orderStamp.compareTo(other.orderStamp)
}

internal fun MokkeryCallScope.toCallTrace(orderStamp: Long): CallTrace {
    val call = call
    return CallTrace(
        instanceId = instanceSpec.id,
        name = call.function.name,
        args = call.args,
        orderStamp = orderStamp
    )
}

