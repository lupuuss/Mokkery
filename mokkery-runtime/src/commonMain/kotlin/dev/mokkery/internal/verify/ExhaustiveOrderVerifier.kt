package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isNotMatching
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer

internal class ExhaustiveOrderVerifier(
    private val callMatcher: CallMatcher,
    private val resultsComposer: TemplateMatchingResultsComposer,
) : Verifier {

    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): Verifier.Result {
        if (callTemplates.size != callTraces.size) return failure(callTraces, callTemplates)
        callTraces
            .zip(callTemplates)
            .filter { (trace, template) -> callMatcher.match(trace, template).isNotMatching }
            .forEach { _ -> return failure(callTraces, callTemplates) }
        return Verifier.Result.Success(callTraces)
    }

    private fun failure(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): Verifier.Result.Failure {
        return Verifier.Result.Failure(Error(resultsComposer.compose(callTraces, callTemplates)))
    }

    data class Error(val results: List<TemplateMatchingResult>) : Verifier.Error
}
