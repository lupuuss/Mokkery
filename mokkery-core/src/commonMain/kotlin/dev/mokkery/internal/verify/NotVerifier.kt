package dev.mokkery.internal.verify

import dev.mokkery.internal.tracing.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.tracing.matches

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
