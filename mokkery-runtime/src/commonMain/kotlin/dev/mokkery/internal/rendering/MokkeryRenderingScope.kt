package dev.mokkery.internal.rendering

import dev.mokkery.MokkeryScope
import dev.mokkery.configurer.MokkeryConfigurer
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.configurer.BaseMokkeryConfigurer
import dev.mokkery.internal.configurer.applyConfigurer
import dev.mokkery.internal.context.tools
import dev.mokkery.rendering.MokkeryRenderingScope

internal fun MokkeryScope.renderingScope(
    block: MokkeryRenderingConfigurer.Block = { }
): MokkeryRenderingScope = MokkeryRendering
    .default
    .plus(mokkeryContext)
    .applyConfigurer(block)
    .let(::MokkeryRenderingScope)

internal fun <R> MokkeryScope.withRenderingScope(
    instances: MokkeryCollection? = null,
    receiverRendering: Boolean = true,
    block: MokkeryRenderingScope.() -> R,
): R = renderingScope {
    if (instances != null) {
        mokkeryCollection(instances)
        useAliases(instances, tools.namesShortener)
    }
    receiverRendering(receiverRendering)
}.let(block)

internal interface MokkeryRenderingConfigurer : MokkeryConfigurer {

    typealias Block = MokkeryRenderingConfigurer.() -> Unit
}

internal fun MokkeryContext.applyConfigurer(block: MokkeryRenderingConfigurer.Block): MokkeryContext {
    return applyConfigurer(::MokkeryRenderingConfigurerImpl, block)
}

private class MokkeryRenderingConfigurerImpl(
    context: MokkeryContext
): BaseMokkeryConfigurer(context), MokkeryRenderingConfigurer
