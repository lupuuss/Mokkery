package dev.mokkery.internal.tracing

import dev.mokkery.MokkeryCallScope
import dev.mokkery.call
import dev.mokkery.context.Function
import dev.mokkery.context.argValues
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.matcher.CallEntry

internal data class CallTrace(
    override val instanceId: MokkeryInstanceId,
    override val functionId: Function.Id,
    override val args: List<Any?>,
    val orderStamp: Long,
): Comparable<CallTrace>, CallEntry {

    override fun compareTo(other: CallTrace) = this.orderStamp.compareTo(other.orderStamp)
}

internal fun MokkeryCallScope.toCallTrace(orderStamp: Long): CallTrace {
    val call = call
    return CallTrace(
        instanceId = instanceSpec.id,
        functionId = call.function.id,
        args = call.argValues,
        orderStamp = orderStamp
    )
}

