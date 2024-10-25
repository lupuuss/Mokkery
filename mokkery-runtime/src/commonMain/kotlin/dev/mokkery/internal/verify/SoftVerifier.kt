package dev.mokkery.internal.verify

import dev.mokkery.internal.utils.failAssertion
import dev.mokkery.internal.calls.CallMatcher
import dev.mokkery.internal.calls.isMatching
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.context.GlobalMokkeryContext
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults
import dev.mokkery.internal.verify.render.TemplateGroupedMatchingResultsRenderer

internal class SoftVerifier(
    private val atLeast: Int,
    private val atMost: Int,
    private val callMatcher: CallMatcher = GlobalMokkeryContext.tools.callMatcher,
    private val matchingResultsRenderer: Renderer<TemplateGroupedMatchingResults> = TemplateGroupedMatchingResultsRenderer()
) : Verifier {

    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        val verified = callTemplates.flatMap { template ->
            val matching = callTraces.filter { callMatcher.match(it, template).isMatching }
            if (matching.size < atLeast || matching.size > atMost) {
                failAssertion {
                    appendMainError(template, matching.size)
                    val results = TemplateGroupedMatchingResults(
                        template = template,
                        calls = callTraces.groupBy { callMatcher.match(it, template) }
                    )
                    append(matchingResultsRenderer.render(results))
                }
            }
            matching
        }
        return verified
    }

    private fun StringBuilder.appendMainError(template: CallTemplate, callsCount: Int) {
        append("Expected ")
        val callsExpectations = when {
            atLeast == atMost -> "exactly $atLeast calls"
            atLeast != 1 && atMost != Int.MAX_VALUE -> "calls count to be in range $atLeast..$atMost"
            atLeast != 1 -> "at least $atLeast calls"
            atMost != Int.MAX_VALUE -> "at most $atMost calls"
            else -> "any call"
        }
        append(callsExpectations)
        if (callsCount == 0) {
            append(", but no matching calls")
        } else {
            append(", but $callsCount occurred")
        }
        appendLine(" for $template!")
    }
}

