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
    block: context(MokkeryRenderingScope)() -> R
): R = renderingScope
    .configured {
        mokkeryCollection(collection)
        useAliases(collection, tools.namesShortener)
        +VerifyRendering.context
    }.let(block)

context(scope: MokkeryRenderingScope)
internal val noMoreCalls
    get() = scope.mokkeryContext.require(VerifyRendering.noMoreCalls)

context(scope: MokkeryRenderingScope)
internal val templateMatchingResults
    get() = scope.mokkeryContext.require(VerifyRendering.templateMatchingResults)

context(scope: MokkeryRenderingScope)
internal val extraUnverifiedCalls
    get() = scope.mokkeryContext.require(VerifyRendering.extraUnverifiedCalls)

context(scope: MokkeryRenderingScope)
internal val matcherStatus
    get() = scope.mokkeryContext.require(VerifyRendering.matcherStatus)

context(scope: MokkeryRenderingScope)
internal val templateGroupedMatchingResults
    get() = scope.mokkeryContext.require(VerifyRendering.templateGroupedMatchingResults)

context(scope: MokkeryRenderingScope)
internal val softVerifierError
    get() = scope.mokkeryContext.require(VerifyRendering.softVerifierError)

context(scope: MokkeryRenderingScope)
internal val notVerifierError
    get() = scope.mokkeryContext.require(VerifyRendering.notVerifierError)


context(scope: MokkeryRenderingScope)
internal val orderVerifierError
    get() = scope.mokkeryContext.require(VerifyRendering.orderVerifierError)

context(scope: MokkeryRenderingScope)
internal val exhaustiveOrderVerifierError
    get() = scope.mokkeryContext.require(VerifyRendering.exhaustiveOrderVerifierError)

context(scope: MokkeryRenderingScope)
internal val exhaustiveSoftVerifierError
    get() = scope.mokkeryContext.require(VerifyRendering.exhaustiveSoftVerifierError)

context(scope: MokkeryRenderingScope)
internal val verifierError
    get() = scope.mokkeryContext.require(VerifyRendering.verifierError)

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
        is ExhaustiveOrderVerifier.Error -> exhaustiveOrderVerifierError.render(value)
        is ExhaustiveSoftVerifier.Error -> exhaustiveSoftVerifierError.render(value)
        is NotVerifier.Error -> notVerifierError.render(value)
        is OrderVerifier.Error -> orderVerifierError.render(value)
        is SoftVerifier.Error -> softVerifierError.render(value)
    }

}
