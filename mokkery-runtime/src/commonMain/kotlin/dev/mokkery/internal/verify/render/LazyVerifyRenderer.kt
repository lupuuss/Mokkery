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
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults
import dev.mokkery.internal.verify.results.TemplateMatchingResult

internal interface LazyVerifyRendererScope {

    val callTraceAliasRenderer: Renderer<CallTrace>

    val callTemplateAliasRenderer: Renderer<CallTemplate>

    val instanceIdAliasRenderer: Renderer<MokkeryInstanceId>

    val templateGroupedMatchingResultsRenderer: Renderer<TemplateGroupedMatchingResults>

    val templateMatchingResultsRenderer: Renderer<List<TemplateMatchingResult>>

    fun <T> pointsRenderer(item: Renderer<T>): Renderer<List<T>>
}

internal inline fun <T> lazyVerifyRenderer(
    nameShortener: NameShortener,
    collection: MokkeryCollection,
    renderers: Renderers,
    crossinline block: LazyVerifyRendererScope.() -> Renderer<T>
): Renderer<T> = Renderer {
    block(LazyVerifyRendererScope(nameShortener, collection, renderers))
        .render(it)
}


internal fun LazyVerifyRendererScope(
    nameShortener: NameShortener,
    collection: MokkeryCollection,
    renderers: Renderers,
): LazyVerifyRendererScope = LazyVerifyRendererScopeImpl(nameShortener, collection, renderers)

private class LazyVerifyRendererScopeImpl(
    private val nameShortener: NameShortener,
    private val collection: MokkeryCollection,
    private val renderers: Renderers,
) : LazyVerifyRendererScope {

    // this way of lazy initialization is more performant than lazy delegate

    private var _aliases: AliasMokkeryCollection? = null
    private val aliases: AliasMokkeryCollection
        get() = inlineLazy(
            getter = { _aliases },
            setter = { _aliases = it },
            initializer = { collection.withShorterNames(nameShortener) }
        )

    private var _instanceIdAliasRenderer: Renderer<MokkeryInstanceId>? = null
    override val instanceIdAliasRenderer
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
                    instanceIdRenderer = instanceIdAliasRenderer
                )
            },
        )

    private var _callTraceAliasRenderer: Renderer<CallTrace>? = null
    override val callTraceAliasRenderer
        get() = inlineLazy(
            getter = { _callTraceAliasRenderer },
            setter = { _callTraceAliasRenderer = it },
            initializer = { renderers.callTrace(callDescriptorRenderer) }
        )

    private var _callTemplateAliasRenderer: Renderer<CallTemplate>? = null
    override val callTemplateAliasRenderer
        get() = inlineLazy(
            getter = { _callTemplateAliasRenderer },
            setter = { _callTemplateAliasRenderer = it },
            initializer = { renderers.callTemplate(callDescriptorRenderer) }
        )


    private var _templateGroupedMatchingResultsRenderer: Renderer<TemplateGroupedMatchingResults>? = null
    override val templateGroupedMatchingResultsRenderer
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
                    traceRenderer = callTraceAliasRenderer,
                    instanceIdRender = instanceIdAliasRenderer
                )
            }
        )

    private var _templateMatchingResultsRenderer: Renderer<List<TemplateMatchingResult>>? = null
    override val templateMatchingResultsRenderer
        get() = inlineLazy(
            getter = { _templateMatchingResultsRenderer },
            setter = { _templateMatchingResultsRenderer = it },
            initializer = {
                TemplateMatchingResultsRenderer(
                    traceRenderer = callTraceAliasRenderer,
                    templateRenderer = callTemplateAliasRenderer,
                )
            }
        )

    override fun <T> pointsRenderer(item: Renderer<T>): Renderer<List<T>> = renderers.points(item = item)
}

private inline fun <T> inlineLazy(
    getter: () -> T,
    setter: (T) -> Unit,
    initializer: () -> T & Any
): T & Any = getter() ?: initializer().also(setter)
