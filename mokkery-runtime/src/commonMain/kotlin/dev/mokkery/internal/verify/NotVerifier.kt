package dev.mokkery.internal.verify

import dev.mokkery.internal.utils.failAssertion
import dev.mokkery.internal.calls.CallMatcher
import dev.mokkery.internal.calls.isMatching
import dev.mokkery.internal.render.PointListRenderer
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.calls.CallTemplate
import dev.mokkery.internal.calls.CallTrace
import dev.mokkery.internal.context.GlobalMokkeryContext
import dev.mokkery.internal.context.tools

internal class NotVerifier(
    private val callMatcher: CallMatcher = GlobalMokkeryContext.tools.callMatcher,
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
