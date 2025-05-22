package dev.mokkery.internal.names

import dev.mokkery.internal.calls.CallTrace

internal interface CallTraceReceiverShortener {

    fun shorten(callTrace: CallTrace): CallTrace
}

internal fun CallTraceReceiverShortener(
    namesShortener: NameShortener,
): CallTraceReceiverShortener {
    return object : CallTraceReceiverShortener {
        override fun shorten(callTrace: CallTrace): CallTrace {
            val id = callTrace.mockId
            val names = namesShortener.shorten(setOf(id.typeName))
            return callTrace.copy(mockId = id.copy(typeName = names.getValue(id.typeName)))
        }
    }
}

internal fun CallTraceReceiverShortener.shortToString(trace: CallTrace) = shorten(trace).toString()
