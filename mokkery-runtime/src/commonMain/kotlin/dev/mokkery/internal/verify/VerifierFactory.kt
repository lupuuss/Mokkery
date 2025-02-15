package dev.mokkery.internal.verify

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.calls.CallMatcher
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.render.PointListRenderer
import dev.mokkery.internal.verify.render.TemplateGroupedMatchingResultsRenderer
import dev.mokkery.internal.verify.render.TemplateMatchingResultsRenderer
import dev.mokkery.internal.verify.render.UnverifiedCallsRenderer
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer
import dev.mokkery.verify.ExhaustiveOrderVerifyMode
import dev.mokkery.verify.ExhaustiveSoftVerifyMode
import dev.mokkery.verify.NotVerifyMode
import dev.mokkery.verify.OrderVerifyMode
import dev.mokkery.verify.SoftVerifyMode
import dev.mokkery.verify.VerifyMode

internal interface VerifierFactory {

    fun create(mode: VerifyMode): Verifier
}

internal fun VerifierFactory(callMatcher: CallMatcher): VerifierFactory {
    return VerifierFactoryImpl(callMatcher)
}

private class VerifierFactoryImpl(private val callMatcher: CallMatcher) : VerifierFactory {

    private val templateMatchingResultsComposer = TemplateMatchingResultsComposer(callMatcher)

    override fun create(mode: VerifyMode): Verifier = when (mode) {
        OrderVerifyMode -> OrderVerifier(
            callMatcher = callMatcher,
            resultsComposer = templateMatchingResultsComposer,
            renderer = TemplateMatchingResultsRenderer()
        )
        ExhaustiveOrderVerifyMode -> ExhaustiveOrderVerifier(
            resultsComposer = templateMatchingResultsComposer,
            callMatcher = callMatcher,
            renderer = TemplateMatchingResultsRenderer(),
        )
        ExhaustiveSoftVerifyMode -> ExhaustiveSoftVerifier(
            callMatcher = callMatcher,
            matchingResultsRenderer = TemplateGroupedMatchingResultsRenderer(),
            unverifiedCallsRenderer = UnverifiedCallsRenderer()
        )
        NotVerifyMode -> NotVerifier(
            callMatcher = callMatcher,
            traceListRenderer = PointListRenderer()
        )
        is SoftVerifyMode -> SoftVerifier(
            atLeast = mode.atLeast,
            atMost = mode.atMost,
            callMatcher = callMatcher,
            matchingResultsRenderer = TemplateGroupedMatchingResultsRenderer()
        )
    }

}
