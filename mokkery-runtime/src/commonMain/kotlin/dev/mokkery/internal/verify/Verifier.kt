package dev.mokkery.internal.verify

import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal sealed interface Verifier {

    fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace>
}

