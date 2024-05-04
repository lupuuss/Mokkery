package dev.mokkery.internal.names

import dev.mokkery.internal.MockUniqueReceiversGenerator
import dev.mokkery.internal.tracing.CallTrace

internal interface CallTraceReceiverShortener {

    fun shorten(callTrace: CallTrace): CallTrace

    companion object : CallTraceReceiverShortener {

        private val receiversGenerator = MockUniqueReceiversGenerator
        private val namesShortener = NameShortener.default

        override fun shorten(callTrace: CallTrace): CallTrace {
            val noIdReceiver = receiversGenerator.extractType(callTrace.receiver)
            val id = callTrace.receiver.removePrefix(noIdReceiver)
            val names = namesShortener.shorten(setOf(noIdReceiver))
            return callTrace.copy(receiver = "${names.getValue(noIdReceiver)}$id")
        }
    }
}

internal fun CallTraceReceiverShortener.shortToString(trace: CallTrace) = shorten(trace).toString()