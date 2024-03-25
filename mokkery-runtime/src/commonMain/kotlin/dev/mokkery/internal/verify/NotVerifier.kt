package dev.mokkery.internal.verify

import dev.mokkery.internal.failAssertion
import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.matcher.isMatching
import dev.mokkery.internal.render.PointListRenderer
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace

internal class NotVerifier(
    private val callMatcher: CallMatcher = CallMatcher(),
    private val traceListRenderer: Renderer<List<CallTrace>> = PointListRenderer(),
) : Verifier {

    override fun verify(
        callTraces: List<CallTrace>,
        callTemplates: List<CallTemplate>
    ): List<CallTrace> {
        callTemplates.forEach { template ->
            callTraces.forEach { trace ->
                if (callMatcher.match(trace, template).isMatching) {
                    failAssertion {
                        val matching = callTraces.filter { callMatcher.match(it, template).isMatching }
                        appendLine("Calls to $template were not expected, but occurred:")
                        append(traceListRenderer.render(matching))
                    }
                }
            }
        }
        return emptyList()
    }
}
