package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.render.RendererFactory
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal class NotVerifier(
    private val callMatcher: CallMatcher,
    private val errorRendererFactory: RendererFactory<Error>,
    ) : Verifier {

    override fun verify(
        callTraces: List<CallTrace>,
        callTemplates: List<CallTemplate>
    ): List<CallTrace> {
        callTemplates.forEach { template ->
            callTraces.forEach { trace ->
                if (callMatcher.match(trace, template).isMatching) {
                    val matching = callTraces.filter { callMatcher.match(it, template).isMatching }
                    val error = Error(template, matching)
                    throw AssertionError(errorRendererFactory.create().render(error))
                }
            }
        }
        return emptyList()
    }

    data class Error(
        val template: CallTemplate,
        val unexpectedCalls: List<CallTrace>
    )
}
