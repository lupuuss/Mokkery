package dev.mokkery.internal.rendering

import dev.mokkery.MokkeryScope
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.context.tools
import dev.mokkery.rendering.MokkeryRenderingScope

internal val MokkeryScope.renderingScope: MokkeryRenderingScope
    get() = MokkeryRenderingScope(MokkeryRendering.default + mokkeryContext)

internal fun <R> MokkeryScope.withRenderingScope(
    instances: MokkeryCollection? = null,
    receiverRendering: Boolean = true,
    block: context(MokkeryRenderingScope)() -> R,
): R = MokkeryRenderingScope(MokkeryRendering.default + mokkeryContext)
    .configured {
        if (instances != null) {
            mokkeryCollection(instances)
            useAliases(instances, tools.namesShortener)
        }
        receiverRendering(receiverRendering)
    }
    .let(block)

internal fun MokkeryRenderingScope.configured(block: RenderingConfigurer.() -> Unit): MokkeryRenderingScope {
    return MokkeryRenderingScope(RenderingConfigurer(mokkeryContext).apply(block).context)
}

internal inline fun <R> withGlobalRenderingScope(block: context(MokkeryRenderingScope)() -> R): R {
    return context(MokkeryScope.global.renderingScope) {
        block()
    }
}
