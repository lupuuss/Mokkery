package dev.mokkery.internal.verify

import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.render.PointListRenderer
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.matcher.CallMatcherFactory
import dev.mokkery.internal.verify.render.MatchersStatusRenderer
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

    fun create(mode: VerifyMode, collection: MokkeryCollection): Verifier
}

internal fun VerifierFactory(callMatcherFactory: CallMatcherFactory): VerifierFactory {
    return VerifierFactoryImpl(callMatcherFactory)
}

private class VerifierFactoryImpl(
    private val callMatcherFactory: CallMatcherFactory
) : VerifierFactory {

    override fun create(mode: VerifyMode, collection: MokkeryCollection): Verifier {
        val callMatcher = callMatcherFactory.create(collection)
        return when (mode) {
            OrderVerifyMode -> OrderVerifier(
                callMatcher = callMatcher,
                resultsComposer = TemplateMatchingResultsComposer(callMatcher),
                renderer = TemplateMatchingResultsRenderer()
            )
            ExhaustiveOrderVerifyMode -> ExhaustiveOrderVerifier(
                resultsComposer = TemplateMatchingResultsComposer(callMatcher),
                callMatcher = callMatcher,
                renderer = TemplateMatchingResultsRenderer(),
            )
            ExhaustiveSoftVerifyMode -> ExhaustiveSoftVerifier(
                callMatcher = callMatcher,
                matchingResultsRenderer = TemplateGroupedMatchingResultsRenderer(
                    MatchersStatusRenderer(DefaultsMaterializer(collection))
                ),
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
                matchingResultsRenderer = TemplateGroupedMatchingResultsRenderer(
                    MatchersStatusRenderer(DefaultsMaterializer(collection))
                )
            )
        }
    }

}
