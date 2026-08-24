package dev.mokkery.test

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.rendering.MokkeryRenderingConfigurer
import dev.mokkery.internal.rendering.applyConfigurer
import dev.mokkery.internal.rendering.mokkeryCollection
import dev.mokkery.rendering.MokkeryRenderingScope

internal fun <R> testRendering(
    context: MokkeryContext = MokkeryContext.Empty,
    collection: MokkeryCollection? = null,
    block: MokkeryRenderingScope.() -> R
): R {
    val ctx = context.applyConfigurer {
        if (collection != null) mokkeryCollection(collection)
    }
    return MokkeryRenderingScope(ctx).let(block)
}

internal fun testRenderingScope(
    block: MokkeryRenderingConfigurer.() -> Unit,
): MokkeryRenderingScope = MokkeryRenderingScope(MokkeryContext.Empty.applyConfigurer(block))
