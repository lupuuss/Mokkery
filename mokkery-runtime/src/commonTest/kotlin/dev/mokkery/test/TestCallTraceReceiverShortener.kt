package dev.mokkery.test

import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.names.CallTraceReceiverShortener

internal class TestCallTraceReceiverShortener : CallTraceReceiverShortener {

    override fun shorten(callTrace: CallTrace): CallTrace = callTrace
}
