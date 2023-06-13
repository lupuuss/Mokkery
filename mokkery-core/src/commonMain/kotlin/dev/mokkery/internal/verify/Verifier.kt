package dev.mokkery.internal.verify

import dev.mokkery.internal.tracing.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

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
