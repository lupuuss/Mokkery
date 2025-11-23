package dev.mokkery.internal.verify

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.matcher.CallMatcherFactory
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.verify.render.ExhaustiveOrderVerifierErrorRenderer
import dev.mokkery.internal.verify.render.ExhaustiveSoftVerifierErrorRenderer
import dev.mokkery.internal.verify.render.NotVerifierErrorRenderer
import dev.mokkery.internal.verify.render.OrderVerifierErrorRenderer
import dev.mokkery.internal.verify.render.SoftVerifierErrorRenderer
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

internal fun VerifierFactory(callMatcherFactory: CallMatcherFactory, nameShortener: NameShortener): VerifierFactory {
    return VerifierFactoryImpl(callMatcherFactory, nameShortener)
}

private class VerifierFactoryImpl(
    private val callMatcherFactory: CallMatcherFactory,
    private val namesShortener: NameShortener,
) : VerifierFactory {

    override fun create(mode: VerifyMode, collection: MokkeryCollection): Verifier {
        val callMatcher = callMatcherFactory.create(collection)
        return when (mode) {
            OrderVerifyMode -> OrderVerifier(
                callMatcher = callMatcher,
                resultsComposer = TemplateMatchingResultsComposer(callMatcher),
                errorRendererFactory = OrderVerifierErrorRenderer.factory(namesShortener, collection)
            )
            ExhaustiveOrderVerifyMode -> ExhaustiveOrderVerifier(
                resultsComposer = TemplateMatchingResultsComposer(callMatcher),
                callMatcher = callMatcher,
                errorRendererFactory = ExhaustiveOrderVerifierErrorRenderer.factory(namesShortener, collection)
            )
            ExhaustiveSoftVerifyMode -> ExhaustiveSoftVerifier(
                callMatcher = callMatcher,
                errorRendererFactory = ExhaustiveSoftVerifierErrorRenderer.factory(namesShortener, collection)
            )
            NotVerifyMode -> NotVerifier(
                callMatcher = callMatcher,
                errorRendererFactory = NotVerifierErrorRenderer.factory(namesShortener, collection)
            )
            is SoftVerifyMode -> SoftVerifier(
                atLeast = mode.atLeast,
                atMost = mode.atMost,
                callMatcher = callMatcher,
                errorRendererFactory = SoftVerifierErrorRenderer.factory(namesShortener, collection)
            )
        }
    }

}
