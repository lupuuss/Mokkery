package dev.mokkery.internal.verify

import dev.mokkery.internal.failAssertion
import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.render.TemplateMatchingResults
import dev.mokkery.internal.verify.render.TemplateMatchingResultsRenderer

internal class SoftVerifier(
    private val atLeast: Int,
    private val atMost: Int,
    private val callMatcher: CallMatcher = CallMatcher(),
    private val matchingResultsRenderer: Renderer<TemplateMatchingResults> = TemplateMatchingResultsRenderer()
) : Verifier {

    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        val verified = callTemplates.flatMap { template ->
            val matching = callTraces.filter { callMatcher.match(it, template).isMatching }
            if (matching.size < atLeast || matching.size > atMost) {
                failAssertion {
                    appendMainError(template, matching.size)
                    val results = TemplateMatchingResults(
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

