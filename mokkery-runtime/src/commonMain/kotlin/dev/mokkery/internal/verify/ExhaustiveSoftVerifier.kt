package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal class ExhaustiveSoftVerifier(private val callMatcher: CallMatcher = CallMatcher()) : Verifier {

    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        val unverifiedTracks = callTraces.toMutableList()
        callTemplates.forEach { template ->
            val matchingCalls = callTraces.filter { callMatcher.match(it, template).isMatching }
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
