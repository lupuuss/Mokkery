package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal class ExhaustiveOrderVerifier(private val callMatcher: CallMatcher = CallMatcher()) : Verifier {

    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        if (callTemplates.size != callTraces.size) {
            failAssertion(callTraces, callTemplates) { "Expected strict order of calls, but not satisfied!" }
        }
        callTraces.zip(callTemplates).forEach { (trace, template) ->
            if (!callMatcher.matches(trace, template)) {
                failAssertion(callTraces, callTemplates) { "Expected strict order of calls, but not satisfied!" }
            }
        }
        return callTraces
    }
}
