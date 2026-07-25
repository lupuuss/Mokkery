package dev.mokkery.internal.verify.render

import dev.mokkery.MokkeryScope
import dev.mokkery.context.memoized
import dev.mokkery.context.require
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.rendering.Renderer
import dev.mokkery.internal.rendering.configured
import dev.mokkery.internal.rendering.mokkeryCollection
import dev.mokkery.internal.rendering.renderingScope
import dev.mokkery.internal.rendering.unaryPlus
import dev.mokkery.internal.rendering.useAliases
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.tracing.CallTrace
import dev.mokkery.internal.verify.ExhaustiveOrderVerifier
import dev.mokkery.internal.verify.ExhaustiveSoftVerifier
import dev.mokkery.internal.verify.NotVerifier
import dev.mokkery.internal.verify.OrderVerifier
import dev.mokkery.internal.verify.SoftVerifier
import dev.mokkery.internal.verify.Verifier
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.rendering.MokkeryRenderingScope

internal fun <R> MokkeryScope.verifyRendering(
    collection: MokkeryCollection,
    block: MokkeryRenderingScope.() -> R
): R = renderingScope
    .configured {
        mokkeryCollection(collection)
        useAliases(collection, tools.namesShortener)
        +VerifyRendering.context
    }.let(block)

internal val MokkeryRenderingScope.noMoreCalls
    get() = mokkeryContext.require(VerifyRendering.noMoreCalls)

internal val MokkeryRenderingScope.templateMatchingResults
    get() = mokkeryContext.require(VerifyRendering.templateMatchingResults)

internal val MokkeryRenderingScope.extraUnverifiedCalls
    get() = mokkeryContext.require(VerifyRendering.extraUnverifiedCalls)

internal val MokkeryRenderingScope.matcherStatus
    get() = mokkeryContext.require(VerifyRendering.matcherStatus)

internal val MokkeryRenderingScope.templateGroupedMatchingResults
    get() = mokkeryContext.require(VerifyRendering.templateGroupedMatchingResults)

internal val MokkeryRenderingScope.softVerifierError
    get() = mokkeryContext.require(VerifyRendering.softVerifierError)

internal val MokkeryRenderingScope.notVerifierError
    get() = mokkeryContext.require(VerifyRendering.notVerifierError)


internal val MokkeryRenderingScope.orderVerifierError
    get() = mokkeryContext.require(VerifyRendering.orderVerifierError)

internal val MokkeryRenderingScope.exhaustiveOrderVerifierError
    get() = mokkeryContext.require(VerifyRendering.exhaustiveOrderVerifierError)

internal val MokkeryRenderingScope.exhaustiveSoftVerifierError
    get() = mokkeryContext.require(VerifyRendering.exhaustiveSoftVerifierError)

internal val MokkeryRenderingScope.verifierError
    get() = mokkeryContext.require(VerifyRendering.verifierError)

internal object VerifyRendering {

    val noMoreCalls by Renderer.key<Pair<MokkeryInstanceId, List<CallTrace>>>()
    val templateMatchingResults by Renderer.key<List<TemplateMatchingResult>>()
    val matcherStatus by Renderer.key<Pair<CallTemplate, CallTrace>>()
    val extraUnverifiedCalls by Renderer.key<List<CallTrace>>()
    val templateGroupedMatchingResults by Renderer.key<TemplateGroupedMatchingResults>()
    val softVerifierError by Renderer.key<SoftVerifier.Error>()
    val notVerifierError by Renderer.key<NotVerifier.Error>()
    val orderVerifierError by Renderer.key<OrderVerifier.Error>()
    val exhaustiveOrderVerifierError by Renderer.key<ExhaustiveOrderVerifier.Error>()
    val exhaustiveSoftVerifierError by Renderer.key<ExhaustiveSoftVerifier.Error>()
    val verifierError by Renderer.key<Verifier.Error>()

    val context by lazy {
        NoMoreCallsErrorRenderer
            .plus(TemplateMatchingResultsRenderer)
            .plus(MatchersStatusRenderer)
            .plus(ExtraUnverifiedCallsRenderer)
            .plus(TemplateGroupedMatchingResultsRenderer)
            .plus(SoftVerifierErrorRenderer)
            .plus(NotVerifierErrorRenderer)
            .plus(OrderVerifierErrorRenderer)
            .plus(ExhaustiveOrderVerifierErrorRenderer)
            .plus(ExhaustiveSoftVerifierErrorRenderer)
            .plus(VerifierErrorRenderer)
            .memoized()
    }
}

private object VerifierErrorRenderer : Renderer<Verifier.Error> {

    override val key get() = VerifyRendering.verifierError

    context(scope: MokkeryRenderingScope)
    override fun render(value: Verifier.Error): String = when (value) {
        is ExhaustiveOrderVerifier.Error -> scope.exhaustiveOrderVerifierError.render(value)
        is ExhaustiveSoftVerifier.Error -> scope.exhaustiveSoftVerifierError.render(value)
        is NotVerifier.Error -> scope.notVerifierError.render(value)
        is OrderVerifier.Error -> scope.orderVerifierError.render(value)
        is SoftVerifier.Error -> scope.softVerifierError.render(value)
    }

}
