package dev.mokkery.internal.verify

import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.tracing.matches

internal object ExhaustiveSoftVerifier : Verifier {
    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        val unverifiedTracks = callTraces.sortedBy { it.orderStamp }.toMutableList()
        callTemplates.forEach { template ->
            val matchingCalls = callTraces.filter { it matches template }
            if (matchingCalls.isEmpty()) {
                failAssertion(callTraces, callTemplates) { "No matching call for $template!" }
            }
            unverifiedTracks.removeAll(matchingCalls.toSet())
        }
        if (unverifiedTracks.isNotEmpty()) {
            failAssertion(callTraces, callTemplates) {
                "Not all calls verified! Unverified calls: $unverifiedTracks"
            }
        }
        return callTraces
    }
}
