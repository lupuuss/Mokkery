package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal class NotVerifier(
    private val callMatcher: CallMatcher,
) : Verifier {

    override fun verify(
        callTraces: List<CallTrace>,
        callTemplates: List<CallTemplate>
    ): Verifier.Result {
        callTemplates.forEach { template ->
            callTraces.forEach { trace ->
                if (callMatcher.match(trace, template).isMatching) {
                    val matching = callTraces.filter { callMatcher.match(it, template).isMatching }
                    return Verifier.Result.Failure(Error(template, matching))
                }
            }
        }
        return Verifier.Result.Success(emptyList())
    }

    data class Error(
        val template: CallTemplate,
        val unexpectedCalls: List<CallTrace>
    ) : Verifier.Error
}
