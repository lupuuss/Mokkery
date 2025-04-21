package dev.mokkery.internal.context

import dev.mokkery.MokkeryCallScope
import dev.mokkery.call
import dev.mokkery.internal.calls.CallTrace

internal fun MokkeryCallScope.toCallTrace(orderStamp: Long): CallTrace {
    val call = call
    return CallTrace(
        mockId = mockSpec.id,
        name = call.function.name,
        args = call.args,
        orderStamp = orderStamp
    )
}
