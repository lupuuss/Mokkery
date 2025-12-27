package dev.mokkery.internal.verify.render

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.defaults.DefaultsMaterializer
import dev.mokkery.internal.names.AliasMokkeryCollection
import dev.mokkery.internal.names.NameShortener
import dev.mokkery.internal.names.withShorterNames
import dev.mokkery.internal.render.CallRenderDescriptor
import dev.mokkery.internal.render.Renderer
import dev.mokkery.internal.render.Renderers
import dev.mokkery.internal.render.callTemplate
import dev.mokkery.internal.render.callTrace
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

internal fun Renderers.noMoreCallsError(
    nameShortener: NameShortener,
    collection: MokkeryCollection
): Renderer<Pair<MokkeryInstanceId, List<CallTrace>>> {
    val aliases = collection.withShorterNames(nameShortener)
    return NoMoreCallsErrorRenderer(
        instanceIdRenderer = instanceId(aliases),
        callsListRenderer = points(item = callTrace(aliases = aliases))
    )
}

internal fun Renderers.verifierError(
    nameShortener: NameShortener,
    collection: MokkeryCollection
): Renderer<Verifier.Error> = VerifierErrorRenderer(nameShortener, collection, this)

private class VerifierErrorRenderer(
    private val nameShortener: NameShortener,
    private val collection: MokkeryCollection,
    private val renderers: Renderers,
) : Renderer<Verifier.Error> {

    // this way of lazy initialization is more performant than lazy delegate
    private var _aliases: AliasMokkeryCollection? = null
    private val aliases: AliasMokkeryCollection
        get() = inlineLazy(
            getter = { _aliases },
            setter = { _aliases = it },
            initializer = { collection.withShorterNames(nameShortener) }
        )

    private var _instanceIdAliasRenderer: Renderer<MokkeryInstanceId>? = null
    private val instanceId
        get() = inlineLazy(
            getter = { _instanceIdAliasRenderer },
            setter = { _instanceIdAliasRenderer = it },
            initializer = { renderers.instanceId(aliases = aliases) }
        )

    private var _callDescriptorRenderer: Renderer<CallRenderDescriptor>? = null
    private val callDescriptorRenderer: Renderer<CallRenderDescriptor>
        get() = inlineLazy(
            getter = { _callDescriptorRenderer },
            setter = { _callDescriptorRenderer = it },
            initializer = {
                renderers.callDescriptor(
                    renderReceiver = true,
                    valueRenderer = renderers.description,
                    matcherRenderer = renderers.toString,
                    instanceIdRenderer = instanceId
                )
            },
        )

    private var _callTraceAliasRenderer: Renderer<CallTrace>? = null
    private val callTrace
        get() = inlineLazy(
            getter = { _callTraceAliasRenderer },
            setter = { _callTraceAliasRenderer = it },
            initializer = { renderers.callTrace(callDescriptorRenderer) }
        )

    private var _callTemplateAliasRenderer: Renderer<CallTemplate>? = null
    private val callTemplate
        get() = inlineLazy(
            getter = { _callTemplateAliasRenderer },
            setter = { _callTemplateAliasRenderer = it },
            initializer = { renderers.callTemplate(callDescriptorRenderer) }
        )


    private var _templateGroupedMatchingResultsRenderer: Renderer<TemplateGroupedMatchingResults>? = null
    private val templateGroupedMatchingResults
        get() = inlineLazy(
            getter = { _templateGroupedMatchingResultsRenderer },
            setter = { _templateGroupedMatchingResultsRenderer = it },
            initializer = {
                TemplateGroupedMatchingResultsRenderer(
                    matchersFailuresRenderer = MatchersStatusRenderer(
                        materializer = DefaultsMaterializer(collection),
                        matcherRenderer = renderers.toString,
                        valueRenderer = renderers.description,
                    ),
                    traceRenderer = callTrace,
                    instanceIdRender = instanceId
                )
            }
        )

    private var _templateMatchingResultsRenderer: Renderer<List<TemplateMatchingResult>>? = null
    private val templateMatchingResults
        get() = inlineLazy(
            getter = { _templateMatchingResultsRenderer },
            setter = { _templateMatchingResultsRenderer = it },
            initializer = {
                TemplateMatchingResultsRenderer(
                    traceRenderer = callTrace,
                    templateRenderer = callTemplate,
                )
            }
        )

    private var _notRenderer: NotVerifierErrorRenderer? = null
    private val notRenderer: NotVerifierErrorRenderer
        get() = inlineLazy(
            getter = { _notRenderer },
            setter = { _notRenderer = it },
            initializer = {
                NotVerifierErrorRenderer(
                    templateRenderer = callTemplate,
                    traceListRenderer = points(callTrace)
                )
            }
        )

    private var _softRenderer: SoftVerifierErrorRenderer? = null
    private val softRenderer: SoftVerifierErrorRenderer
        get() = inlineLazy(
            getter = { _softRenderer },
            setter = { _softRenderer = it },
            initializer = {
                SoftVerifierErrorRenderer(
                    templateRenderer = callTemplate,
                    matchingResultsRenderer = templateGroupedMatchingResults,
                )
            }
        )

    private var _exhaustiveOrderRenderer: ExhaustiveOrderVerifierErrorRenderer? = null
    private val exhaustiveOrderRenderer: ExhaustiveOrderVerifierErrorRenderer
        get() = inlineLazy(
            getter = { _exhaustiveOrderRenderer },
            setter = { _exhaustiveOrderRenderer = it },
            initializer = {
                ExhaustiveOrderVerifierErrorRenderer(
                    matchingResultsRenderer = templateMatchingResults
                )
            }
        )

    private var _exhaustiveSoftRenderer: ExhaustiveSoftVerifierErrorRenderer? = null
    private val exhaustiveSoftRenderer: ExhaustiveSoftVerifierErrorRenderer
        get() = inlineLazy(
            getter = { _exhaustiveSoftRenderer },
            setter = { _exhaustiveSoftRenderer = it },
            initializer = {
                ExhaustiveSoftVerifierErrorRenderer(
                    templateRenderer = callTemplate,
                    matchingResultsRenderer = templateGroupedMatchingResults,
                    unverifiedCallsRenderer = ExtraUnverifiedCallsRenderer(
                        traceListRenderer = points(callTrace)
                    ),
                )
            }
        )

    private var _orderRenderer: OrderVerifierErrorRenderer? = null
    private val orderRenderer: OrderVerifierErrorRenderer
        get() = inlineLazy(
            getter = { _orderRenderer },
            setter = { _orderRenderer = it },
            initializer = {
                OrderVerifierErrorRenderer(
                    templateRenderer = callTemplate,
                    matchingResultsRenderer = templateMatchingResults
                )
            }
        )

    override fun render(value: Verifier.Error): String = when (value) {
        is ExhaustiveOrderVerifier.Error -> exhaustiveOrderRenderer.render(value)
        is ExhaustiveSoftVerifier.Error -> exhaustiveSoftRenderer.render(value)
        is NotVerifier.Error -> notRenderer.render(value)
        is OrderVerifier.Error -> orderRenderer.render(value)
        is SoftVerifier.Error -> softRenderer.render(value)
    }

    private fun <T> points(item: Renderer<T>): Renderer<List<T>> = renderers.points(item = item)
}

private inline fun <T> inlineLazy(
    getter: () -> T,
    setter: (T) -> Unit,
    initializer: () -> T & Any
): T & Any = getter() ?: initializer().also(setter)
