package dev.mokkery.internal.verify.results

import dev.mokkery.internal.calls.CallMatcher
import dev.mokkery.internal.calls.isMatching
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace

internal interface TemplateMatchingResultsComposer {

    fun compose(traces: List<CallTrace>, templates: List<CallTemplate>): List<TemplateMatchingResult>
}

internal fun TemplateMatchingResultsComposer(callMatcher: CallMatcher): TemplateMatchingResultsComposer {
    return TemplateMatchingResultsComposerImpl(callMatcher)
}

private class TemplateMatchingResultsComposerImpl(
    private val callMatcher: CallMatcher,
) : TemplateMatchingResultsComposer {
    override fun compose(traces: List<CallTrace>, templates: List<CallTemplate>): List<TemplateMatchingResult> {
        val results = ArrayList<TemplateMatchingResult>(traces.size + templates.size)
        var currentTraces: List<CallTrace> = traces
        for (template in templates) {
            val matchingCallIndex = currentTraces.indexOfFirst { callMatcher.match(it, template).isMatching }
            if (matchingCallIndex == -1) {
                results.add(TemplateMatchingResult.NoMatch(template))
                continue
            }
            currentTraces.subList(0, matchingCallIndex).mapTo(results, TemplateMatchingResult::UnverifiedCall)
            results.add(TemplateMatchingResult.Matching(currentTraces[matchingCallIndex], template))
            currentTraces = currentTraces.subList(matchingCallIndex + 1, currentTraces.size)
        }
        currentTraces.mapTo(results, TemplateMatchingResult::UnverifiedCall)
        results.trimToSize()
        return results
    }
}
