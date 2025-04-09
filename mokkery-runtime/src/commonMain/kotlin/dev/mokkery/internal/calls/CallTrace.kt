package dev.mokkery.internal.calls

import dev.mokkery.context.CallArgument
import dev.mokkery.internal.MockId
import dev.mokkery.internal.utils.callFunctionToString
import dev.mokkery.internal.utils.callToString

internal data class CallTrace(
    val mockId: MockId,
    val name: String,
    val args: List<CallArgument>,
    val orderStamp: Long,
) {
    override fun toString(): String = callToString(mockId, name, args)

    fun toStringNoMockId() = callFunctionToString(name, args)
}

