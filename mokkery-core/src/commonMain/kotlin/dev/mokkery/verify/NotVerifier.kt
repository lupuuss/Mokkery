package dev.mokkery.verify

import dev.mokkery.tracking.CallTemplate
import dev.mokkery.tracking.CallTrace
import dev.mokkery.tracking.matches

internal object NotVerifier : Verifier {
    override fun verify(
        callTraces: List<CallTrace>,
        callTemplates: List<CallTemplate>
    ): List<CallTrace> {
        callTemplates.forEach { template ->
            callTraces.forEach {
                if (it matches template) {
                    failAssertion(callTraces, callTemplates) {
                        "Call of $template was not expected, but it occurred as $it"
                    }
                }
            }
        }
        return emptyList()
    }
}
