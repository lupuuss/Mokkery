package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults

internal class ExhaustiveSoftVerifier(
    private val callMatcher: CallMatcher,
    private val errorRenderer: Renderer<Error>
) : Verifier {

    override fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): List<CallTrace> {
        val unverifiedCalls = callTraces.toMutableList()
        callTemplates.forEach { template ->
            val matching = callTraces.filter { callMatcher.match(it, template).isMatching }
            if (matching.isEmpty()) {
                val matchingResults = callTraces.groupBy { callMatcher.match(it, template) }
                val results = TemplateGroupedMatchingResults(template = template, calls = matchingResults)
                fail(Error.NoMatch(results))
            }
            unverifiedCalls.removeAll(matching.toSet())
        }
        if (unverifiedCalls.isNotEmpty()) fail(Error.UnverifiedCalls(unverifiedCalls))
        return callTraces
    }

    private fun fail(error: Error): Nothing = throw AssertionError(errorRenderer.render(error))

    sealed class Error {
        data class NoMatch(val templateMatchingResults: TemplateGroupedMatchingResults) : Error()
        data class UnverifiedCalls(val calls: List<CallTrace>) : Error()
    }
}
