package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer

internal class OrderVerifier(
    private val callMatcher: CallMatcher,
    private val resultsComposer: TemplateMatchingResultsComposer,
    private val errorRenderer: Renderer<Error>,
) : Verifier {

    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        var currentTraces = callTraces.toMutableList()
        val verifiedTraces = mutableListOf<CallTrace>()
        callTemplates.forEachIndexed { templateIndex, template ->
            val matchingTraceIndex = currentTraces.indexOfFirst { callMatcher.match(it, template).isMatching }
            if (matchingTraceIndex == -1) {
                val error = Error(
                    failedAt = template,
                    failedIndex = templateIndex,
                    results = resultsComposer.compose(callTraces, callTemplates)
                )
                throw AssertionError(errorRenderer.render(error))
            }
            verifiedTraces.add(currentTraces[matchingTraceIndex])
            currentTraces = currentTraces.subList(matchingTraceIndex + 1, currentTraces.size)
        }
        return verifiedTraces
    }

    data class Error(
        val failedAt: CallTemplate,
        val failedIndex: Int,
        val results: List<TemplateMatchingResult>
    )
}
