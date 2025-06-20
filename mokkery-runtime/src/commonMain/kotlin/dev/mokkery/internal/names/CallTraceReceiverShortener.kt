package dev.mokkery.internal.names

import dev.mokkery.internal.tracing.CallTrace

internal interface CallTraceReceiverShortener {

    fun shorten(callTrace: CallTrace): CallTrace
}

internal fun CallTraceReceiverShortener(
    namesShortener: NameShortener,
): CallTraceReceiverShortener {
    return object : CallTraceReceiverShortener {
        override fun shorten(callTrace: CallTrace): CallTrace {
            val id = callTrace.instanceId
            val names = namesShortener.shorten(setOf(id.typeName))
            return callTrace.copy(instanceId = id.copy(typeName = names.getValue(id.typeName)))
        }
    }
}

internal fun CallTraceReceiverShortener.shortToString(trace: CallTrace) = shorten(trace).toString()
