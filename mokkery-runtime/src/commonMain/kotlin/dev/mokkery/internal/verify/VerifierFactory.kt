package dev.mokkery.internal.verify

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.matcher.CallMatcherFactory
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyModeInternals.Exhaustive
import dev.mokkery.verify.VerifyModeInternals.ExhaustiveOrder
import dev.mokkery.verify.VerifyModeInternals.Not
import dev.mokkery.verify.VerifyModeInternals.Order
import dev.mokkery.verify.VerifyModeInternals.Soft

internal interface VerifierFactory {

    fun create(mode: VerifyMode, collection: MokkeryCollection): Verifier
}

internal fun VerifierFactory(
    callMatcherFactory: CallMatcherFactory,
): VerifierFactory = VerifierFactoryImpl(callMatcherFactory)

private class VerifierFactoryImpl(
    private val callMatcherFactory: CallMatcherFactory,
) : VerifierFactory {

    override fun create(mode: VerifyMode, collection: MokkeryCollection): Verifier {
        val callMatcher = callMatcherFactory.create(collection)
        return when (mode) {
            is Soft -> SoftVerifier(atLeast = mode.atLeast, atMost = mode.atMost, callMatcher = callMatcher)
            Exhaustive -> ExhaustiveSoftVerifier(callMatcher = callMatcher)
            Not -> NotVerifier(callMatcher = callMatcher)
            Order -> OrderVerifier(
                callMatcher = callMatcher,
                resultsComposer = TemplateMatchingResultsComposer(callMatcher)
            )
            ExhaustiveOrder -> ExhaustiveOrderVerifier(
                resultsComposer = TemplateMatchingResultsComposer(callMatcher),
                callMatcher = callMatcher,
            )
        }
    }

}
