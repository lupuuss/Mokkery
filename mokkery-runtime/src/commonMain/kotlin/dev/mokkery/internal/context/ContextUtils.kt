package dev.mokkery.internal.context

import dev.mokkery.MokkeryCallScope
import dev.mokkery.call
import dev.mokkery.internal.tracing.CallTrace

internal fun MokkeryCallScope.toCallTrace(orderStamp: Long): CallTrace {
    val call = call
    return CallTrace(
        instanceId = instanceSpec.id,
        name = call.function.name,
        args = call.args,
        orderStamp = orderStamp
    )
}
