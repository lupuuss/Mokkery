package dev.mokkery.internal.verify

import dev.mokkery.internal.failAssertion
import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.render.TemplateMatchingResultsRenderer
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer

internal class OrderVerifier(
    private val callMatcher: CallMatcher = CallMatcher(),
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
