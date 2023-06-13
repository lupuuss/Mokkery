package dev.mokkery.internal.verify

import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.tracing.doesNotMatch

internal object ExhaustiveOrderVerifier : Verifier {
    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        val sortedTracks = callTraces.sortedBy { it.orderStamp }
        if (callTemplates.size != callTraces.size) {
            failAssertion(callTraces, callTemplates) { "Expected strict order of calls, but not satisfied!" }
        }
        sortedTracks.zip(callTemplates).forEach { (trace, template) ->
            if (trace doesNotMatch template) {
                failAssertion(callTraces, callTemplates) { "Expected strict order of calls, but not satisfied!" }
            }
        }
        return callTraces
    }
}
