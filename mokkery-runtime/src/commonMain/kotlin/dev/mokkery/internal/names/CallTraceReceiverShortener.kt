package dev.mokkery.internal.names

import dev.mokkery.internal.calls.CallTrace

internal interface CallTraceReceiverShortener {

    fun shorten(callTrace: CallTrace): CallTrace
}

internal fun CallTraceReceiverShortener(
    receiversGenerator: MokkeryInstanceIdGenerator,
    namesShortener: NameShortener,
): CallTraceReceiverShortener {
    return object : CallTraceReceiverShortener {
        override fun shorten(callTrace: CallTrace): CallTrace {
            val noIdReceiver = receiversGenerator.extractType(callTrace.receiver)
            val id = callTrace.receiver.removePrefix(noIdReceiver)
            val names = namesShortener.shorten(setOf(noIdReceiver))
            return callTrace.copy(receiver = "${names.getValue(noIdReceiver)}$id")
        }
    }
}

internal fun CallTraceReceiverShortener.shortToString(trace: CallTrace) = shorten(trace).toString()
