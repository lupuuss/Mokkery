package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isNotMatching
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer

internal class ExhaustiveOrderVerifier(
    private val callMatcher: CallMatcher,
    private val resultsComposer: TemplateMatchingResultsComposer,
    private val errorRenderer: Renderer<Error>,
) : Verifier {

    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        if (callTemplates.size != callTraces.size) fail(callTraces, callTemplates)
        callTraces
            .zip(callTemplates)
            .filter { (trace, template) -> callMatcher.match(trace, template).isNotMatching }
            .forEach { _ -> fail(callTraces, callTemplates) }
        return callTraces
    }

    private fun fail(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): Nothing {
        val error = Error(resultsComposer.compose(callTraces, callTemplates))
        throw AssertionError(errorRenderer.render(error))
    }


    data class Error(val results: List<TemplateMatchingResult>)
}
