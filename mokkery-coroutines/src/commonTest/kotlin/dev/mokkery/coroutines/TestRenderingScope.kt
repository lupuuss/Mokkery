package dev.mokkery.coroutines

import dev.mokkery.context.MokkeryContext
import dev.mokkery.rendering.MokkeryRenderingScope

internal fun <R> testRendering(
    context: MokkeryContext = MokkeryContext.Empty,
    block: MokkeryRenderingScope.() -> R
): R {
    return MokkeryRenderingScope(context).let(block)
}

internal fun testRenderingScope(
    context: MokkeryContext = MokkeryContext.Empty,
): MokkeryRenderingScope {
    return MokkeryRenderingScope(context)
}

private fun MokkeryRenderingScope(context: MokkeryContext) = object : MokkeryRenderingScope {
    override val mokkeryContext: MokkeryContext = context
}
