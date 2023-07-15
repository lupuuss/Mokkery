package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal class SoftVerifier(
    private val atLeast: Int,
    private val atMost: Int,
    private val callMatcher: CallMatcher = CallMatcher(),
) : Verifier {

    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        val verified = callTemplates.flatMap { template ->
            val matching = callTraces.filter { callMatcher.matches(it, template) }
            if (matching.size < atLeast || matching.size > atMost) {
                failAssertion(callTraces, callTemplates) {
                    "Expected calls count to be in range $atLeast..${atMost.toReadableString()}, but is ${matching.size} for $template!"
                }
            }
            matching
        }
        return verified
    }

    private fun Int.toReadableString() = if (this == Int.MAX_VALUE) "Int.MAX_VALUE" else toString()
}

