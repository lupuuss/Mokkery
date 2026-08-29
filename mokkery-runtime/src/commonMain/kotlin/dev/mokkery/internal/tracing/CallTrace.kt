package dev.mokkery.internal.tracing

import dev.mokkery.MokkeryCallScope
import dev.mokkery.call
import dev.mokkery.context.Function
import dev.mokkery.context.argValues
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.matcher.CallEntry
import kotlin.jvm.JvmInline

internal class CallTrace(
    /**
     * Uniquely identifies it and determines its order.
     */
    val id: Id,
    override val instanceId: MokkeryInstanceId,
    override val functionId: Function.Id,
    override val args: List<Any?>,
): Comparable<CallTrace>, CallEntry {

    override fun compareTo(other: CallTrace) = this.id.value.compareTo(other.id.value)

    override fun equals(other: Any?) = this === other || other is CallTrace && id == other.id

    override fun hashCode() = id.hashCode()

    override fun toString() = "CallTrace(id=$id, instanceId=$instanceId, functionId=$functionId, args=$args)"

    @JvmInline
    value class Id(val value: Long) {

        override fun toString(): String = "CallTrace.Id($value)"
    }
}

internal fun MokkeryCallScope.toCallTrace(id: CallTrace.Id): CallTrace {
    val call = call
    return CallTrace(
        id = id,
        instanceId = instanceSpec.id,
        functionId = call.function.id,
        args = call.argValues,
    )
}

