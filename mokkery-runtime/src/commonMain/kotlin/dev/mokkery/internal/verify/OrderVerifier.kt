package dev.mokkery.internal.verify

import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.tracing.matches

internal object OrderVerifier : Verifier {
    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        var currentTraces = callTraces.toMutableList()
        var previousTemplate: CallTemplate? = null
        val verifiedTraces = mutableListOf<CallTrace>()
        callTemplates.forEach { template ->
            val index = currentTraces.indexOfFirst { it matches template }
            if (index == -1) {
                failAssertion(callTraces, callTemplates) {
                    if (previousTemplate == null) {
                        "Expected calls in specified order, but missing call for $template! \n"
                    } else {
                        "Expected calls in specified order, but missing call for $template after $previousTemplate!"
                    }
                }
            }
            verifiedTraces.add(currentTraces[index])
            previousTemplate = template
            currentTraces = currentTraces.subList(index + 1, currentTraces.size)
        }
        return verifiedTraces
    }
}
