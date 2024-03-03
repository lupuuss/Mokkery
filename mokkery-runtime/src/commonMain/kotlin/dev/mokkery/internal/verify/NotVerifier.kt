package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal class NotVerifier(private val callMatcher: CallMatcher = CallMatcher()) : Verifier {

    override fun verify(
        callTraces: List<CallTrace>,
        callTemplates: List<CallTemplate>
    ): List<CallTrace> {
        callTemplates.forEach { template ->
            callTraces.forEach {
                if (callMatcher.match(it, template).isMatching) {
                    failAssertion(callTraces, callTemplates) {
                        "Call of $template was not expected, but it occurred as $it"
                    }
                }
            }
        }
        return emptyList()
    }
}
