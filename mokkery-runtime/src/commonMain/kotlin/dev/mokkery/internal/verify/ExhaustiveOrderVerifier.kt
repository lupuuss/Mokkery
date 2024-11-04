package dev.mokkery.internal.verify

import dev.mokkery.internal.utils.failAssertion
import dev.mokkery.internal.calls.CallMatcher
import dev.mokkery.internal.calls.isNotMatching
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.context.GlobalMokkeryContext
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.verify.render.TemplateMatchingResultsRenderer
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer

internal class ExhaustiveOrderVerifier(
    private val callMatcher: CallMatcher = GlobalMokkeryContext.tools.callMatcher,
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
