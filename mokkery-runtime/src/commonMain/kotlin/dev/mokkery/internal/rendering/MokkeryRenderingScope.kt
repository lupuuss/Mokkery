package dev.mokkery.internal.rendering

import dev.mokkery.MokkeryCallScope
import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.MokkeryScope
import dev.mokkery.configurer.MokkeryConfigurer
import dev.mokkery.context.Function
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MokkeryInstanceId
import dev.mokkery.internal.configurer.BaseMokkeryConfigurer
import dev.mokkery.internal.configurer.applyConfigurer
import dev.mokkery.internal.context.functions
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.getScope
import dev.mokkery.internal.mokkeryRuntimeError
import dev.mokkery.internal.templating.templatingRegistry
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.templating.MokkeryTemplatingScope

internal fun MokkeryScope.renderingScope(
    block: MokkeryRenderingConfigurer.Block = { }
): MokkeryRenderingScope = MokkeryRendering
    .default
    .plus(mokkeryContext)
    .applyConfigurer(block)
    .let(::MokkeryRenderingScope)

internal fun <R> MokkeryTemplatingScope.withRenderingScope(
    receiverRendering: Boolean = true,
    useAliasing: Boolean = true,
    block: MokkeryRenderingScope.() -> R,
): R = withRenderingScope(
    instances = this.templatingRegistry.collection,
    useAliasing = useAliasing,
    receiverRendering = receiverRendering,
    block = block
)


internal fun <R> MokkeryInstanceScope.withRenderingScope(
    receiverRendering: Boolean = true,
    useShortening: Boolean = true,
    block: MokkeryRenderingScope.() -> R,
): R = withRenderingScope(
    instances = this.instanceSpec.collection,
    receiverRendering = receiverRendering,
    useAliasing = useShortening,
    block = block
)

internal fun <R> MokkeryCallScope.withRenderingScope(
    receiverRendering: Boolean = true,
    useAliasing: Boolean = true,
    block: MokkeryRenderingScope.() -> R,
): R = withRenderingScope(
    instances = this.instanceSpec.collection,
    receiverRendering = receiverRendering,
    useAliasing = useAliasing,
    block = block
)

private fun <R> MokkeryScope.withRenderingScope(
    instances: MokkeryCollection,
    receiverRendering: Boolean,
    useAliasing: Boolean,
    block: MokkeryRenderingScope.() -> R,
): R = renderingScope {
    mokkeryCollection(instances)
    if (useAliasing) useAliases(instances, tools.namesShortener)
    receiverRendering(receiverRendering)
}.let(block)

internal interface MokkeryRenderingConfigurer : MokkeryConfigurer {

    typealias Block = MokkeryRenderingConfigurer.() -> Unit
}

internal fun MokkeryContext.applyConfigurer(block: MokkeryRenderingConfigurer.Block): MokkeryContext {
    return applyConfigurer(::MokkeryRenderingConfigurerImpl, block)
}

internal fun MokkeryRenderingScope.function(
    instanceId: MokkeryInstanceId,
    functionId: Function.Id
): Function = mokkeryCollection
    .getScope(instanceId)
    .functions[functionId]

private class MokkeryRenderingConfigurerImpl(
    context: MokkeryContext
) : BaseMokkeryConfigurer(context), MokkeryRenderingConfigurer
