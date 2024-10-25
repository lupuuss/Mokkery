package dev.mokkery.internal.verify

import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace

internal sealed interface Verifier {

    fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace>
}

