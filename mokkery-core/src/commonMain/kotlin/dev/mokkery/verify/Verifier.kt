package dev.mokkery.verify

import dev.mokkery.tracking.CallTemplate
import dev.mokkery.tracking.CallTrace
import dev.mokkery.tracking.doesNotMatch
import dev.mokkery.tracking.matches

internal sealed interface Verifier {

    fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace>
}

internal fun failAssertion(
    callTraces: List<CallTrace>,
    callTemplates: List<CallTemplate>,
    message: () -> String
): Nothing = throw AssertionError("${message()}\n" + mockStateMessage(callTraces, callTemplates))

private fun mockStateMessage(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): String {
    return "Expected patterns: \n\t${callTemplates.joinToString(separator = "\n\t")}\n" +
            "Actual calls: \n\t${callTraces.joinToString(separator = "\n\t")}\n"
}
