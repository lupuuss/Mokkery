package dev.mokkery.internal.verify

import dev.mokkery.internal.utils.failAssertion
import dev.mokkery.internal.calls.CallMatcher
import dev.mokkery.internal.calls.isMatching
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.context.GlobalMokkeryContext
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.verify.render.TemplateMatchingResultsRenderer
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer

internal class OrderVerifier(
    private val callMatcher: CallMatcher = GlobalMokkeryContext.tools.callMatcher,
    private val resultsComposer: TemplateMatchingResultsComposer = TemplateMatchingResultsComposer(callMatcher),
    private val renderer: Renderer<List<TemplateMatchingResult>> = TemplateMatchingResultsRenderer()
) : Verifier {

    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        var currentTraces = callTraces.toMutableList()
        val verifiedTraces = mutableListOf<CallTrace>()
        callTemplates.forEachIndexed { templateIndex, template ->
            val matchingTraceIndex = currentTraces.indexOfFirst { callMatcher.match(it, template).isMatching }
            if (matchingTraceIndex == -1) {
                failAssertion {
                    appendMainError(template, templateIndex)
                    appendRenderedResults(callTraces, callTemplates)
                }
            }
            verifiedTraces.add(currentTraces[matchingTraceIndex])
            currentTraces = currentTraces.subList(matchingTraceIndex + 1, currentTraces.size)
        }
        return verifiedTraces
    }

    private fun StringBuilder.appendMainError(
        template: CallTemplate,
        templateIndex: Int
    ) {
        append("Expected calls in specified order but not satisfied! ")
        appendLine("Failed at ${templateIndex + 1}. $template!")
    }

    private fun StringBuilder.appendRenderedResults(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>) {
        val results = resultsComposer.compose(callTraces, callTemplates)
        append(renderer.render(results))
    }
}
