package dev.mokkery.internal.verify

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.matcher.CallMatcher
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verify.VerifyModeInternals.Exhaustive
import dev.mokkery.verify.VerifyModeInternals.ExhaustiveOrder
import dev.mokkery.verify.VerifyModeInternals.Not
import dev.mokkery.verify.VerifyModeInternals.Order
import dev.mokkery.verify.VerifyModeInternals.Soft

internal sealed interface Verifier {

    fun verify(callTraces: List<CallTrace>, callTemplates: List<CallTemplate>): Result

    sealed interface Result {
        data class Success(val verified: List<CallTrace>) : Result
        data class Failure(val error: Error) : Result
    }

    sealed interface Error


    fun interface Factory {

        fun create(mode: VerifyMode, collection: MokkeryCollection): Verifier

        companion object {

            fun default(callMatcher: CallMatcher.Factory): Factory = VerifierFactoryImpl(callMatcher)
        }
    }
}

private class VerifierFactoryImpl(
    private val callMatcherFactory: CallMatcher.Factory,
) : Verifier.Factory {

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
