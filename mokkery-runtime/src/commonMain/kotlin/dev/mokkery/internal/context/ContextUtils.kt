package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.call
import dev.mokkery.context.require
import dev.mokkery.internal.id
import dev.mokkery.internal.calls.CallTrace

internal fun MokkeryContext.toTrace(orderStamp: Long): CallTrace {
    val call = call
    return CallTrace(
        receiver = require(CurrentMokkeryInstance).value.id,
        name = call.function.name,
        args = call.args,
        orderStamp = orderStamp
    )
}
