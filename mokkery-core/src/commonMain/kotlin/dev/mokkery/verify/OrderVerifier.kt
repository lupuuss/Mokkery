package dev.mokkery.verify

import dev.mokkery.tracking.CallTemplate
import dev.mokkery.tracking.CallTrace
import dev.mokkery.tracking.matches

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
            currentTraces = currentTraces.subList(index, currentTraces.lastIndex)
        }
        return verifiedTraces
    }
}
