package dev.mokkery.internal.rendering

import dev.mokkery.MokkeryScope
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.context.tools
import dev.mokkery.rendering.MokkeryRenderingScope

internal fun MokkeryScope.renderingScope(
    block: MokkeryRenderingConfigurer.() -> Unit = { }
): MokkeryRenderingScope = MokkeryRendering
    .default
    .plus(mokkeryContext)
    .let(::MokkeryRenderingConfigurer)
    .apply(block)
    .mokkeryContext
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
