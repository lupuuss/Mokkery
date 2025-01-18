package dev.mokkery.internal.context

import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.interceptor.call
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.id

internal fun MokkeryCallScope.toCallTrace(orderStamp: Long): CallTrace {
    val call = call
    return CallTrace(
        receiver = currentMokkeryInstance.id,
        name = call.function.name,
        args = call.args,
        orderStamp = orderStamp
    )
}
