package dev.mokkery.test

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.rendering.MokkeryRenderingConfigurer
import dev.mokkery.rendering.MokkeryRenderingScope

internal fun <R> testRendering(
    context: MokkeryContext = MokkeryContext.Empty,
    block: MokkeryRenderingScope.() -> R
): R {
    return MokkeryRenderingScope(context).let(block)
}

internal fun testRenderingScope(
    block: MokkeryRenderingConfigurer.() -> Unit,
): MokkeryRenderingScope {
    return MokkeryRenderingScope(MokkeryRenderingConfigurer(MokkeryContext.Empty).apply(block).mokkeryContext)
}
