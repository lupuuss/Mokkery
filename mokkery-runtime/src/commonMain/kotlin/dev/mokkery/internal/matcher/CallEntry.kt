package dev.mokkery.internal.matcher

import dev.mokkery.MokkeryCallScope
import dev.mokkery.call
import dev.mokkery.context.Function
import dev.mokkery.context.argValues
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.context.instanceSpec

internal interface CallEntry {
    val instanceId: MokkeryInstanceId
    val functionId: Function.Id
    val args: List<Any?>
}

internal fun CallEntry(
    instanceId: MokkeryInstanceId,
    functionId: Function.Id,
    args: List<Any?>
): CallEntry = CallEntryImpl(instanceId, functionId, args)

internal fun MokkeryCallScope.asCallEntry(): CallEntry = ScopeCallEntryView(this)

private class CallEntryImpl(
    override val instanceId: MokkeryInstanceId,
    override val functionId: Function.Id,
    override val args: List<Any?>,
) : AbstractCallEntry()

private class ScopeCallEntryView(
    scope: MokkeryCallScope,
) : AbstractCallEntry() {

    private val call = scope.call

    override val instanceId = scope.instanceSpec.id

    override val functionId: Function.Id
        get() = call.function.id
    override val args: List<Any?>
        get() = call.argValues
}


private abstract class AbstractCallEntry : CallEntry {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractCallEntry) return false
        if (instanceId != other.instanceId) return false
        if (functionId != other.functionId) return false
        if (args != other.args) return false
        return true
    }

    override fun hashCode(): Int {
        var result = instanceId.hashCode()
        result = 31 * result + functionId.hashCode()
        result = 31 * result + args.hashCode()
        return result
    }
}
