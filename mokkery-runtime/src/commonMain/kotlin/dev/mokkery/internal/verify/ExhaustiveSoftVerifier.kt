package dev.mokkery.internal.verify

import dev.mokkery.internal.failAssertion
import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.render.TemplateMatchingResults
import dev.mokkery.internal.verify.render.TemplateMatchingResultsRenderer
import dev.mokkery.internal.verify.render.UnverifiedCallsRenderer

internal class ExhaustiveSoftVerifier(
    private val callMatcher: CallMatcher = CallMatcher(),
    private val matchingResultsRenderer: Renderer<TemplateMatchingResults> = TemplateMatchingResultsRenderer(),
    private val unverifiedCallsRenderer: Renderer<List<CallTrace>> = UnverifiedCallsRenderer()
) : Verifier {

    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        val unverifiedCalls = callTraces.toMutableList()
        callTemplates.forEach { template ->
            val matching = callTraces.filter { callMatcher.match(it, template).isMatching }
            if (matching.isEmpty()) {
                failAssertion {
                    appendLine("No matching call for $template!")
                    val results = TemplateMatchingResults(
                        template = template,
                        calls = callTraces.groupBy { callMatcher.match(it, template) }
                    )
                    append(matchingResultsRenderer.render(results))
                }
            }
            unverifiedCalls.removeAll(matching.toSet())
        }
        if (unverifiedCalls.isNotEmpty()) {
            failAssertion {
                append(unverifiedCallsRenderer.render(unverifiedCalls))
            }
        }
        return callTraces
    }
}
