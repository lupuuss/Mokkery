package dev.mokkery.internal.verify

import dev.mokkery.internal.failAssertion
import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isNotMatching
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.render.TemplateMatchingResultsRenderer
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer

internal class ExhaustiveOrderVerifier(
    private val callMatcher: CallMatcher = CallMatcher(),
    private val resultsComposer: TemplateMatchingResultsComposer = TemplateMatchingResultsComposer(callMatcher),
    private val renderer: Renderer<List<TemplateMatchingResult>> = TemplateMatchingResultsRenderer()
) : Verifier {

    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        if (callTemplates.size != callTraces.size) {
            failAssertion {
                appendMainError()
                appendRenderedResults(callTraces, callTemplates)
            }
        }
        callTraces.zip(callTemplates).forEach { (trace, template) ->
            if (callMatcher.match(trace, template).isNotMatching) {
                failAssertion {
                    appendMainError()
                    appendRenderedResults(callTraces, callTemplates)
                }
            }
        }
        return callTraces
    }

    private fun StringBuilder.appendMainError() {
        appendLine("Expected strict order of calls without unverified ones, but not satisfied!")
    }

    private fun StringBuilder.appendRenderedResults(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>) {
        val results = resultsComposer.compose(callTraces, callTemplates)
        append(renderer.render(results))
    }
}
