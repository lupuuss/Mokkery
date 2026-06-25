package dev.mokkery.rendering

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.mokkeryInternals

/**
 * Scope that carries the [dev.mokkery.context.MokkeryContext] with [dev.mokkery.internal.rendering.Renderer]s and
 * related tools. They are used to provide consistent output across different Mokkery components.
 *
 * Currently, only few renderers are publicly available.
 * They are not stable and available only under [mokkeryInternals].
 */
public interface MokkeryRenderingScope : MokkeryScope

internal fun MokkeryRenderingScope(
    context: MokkeryContext
): MokkeryRenderingScope = object : MokkeryRenderingScope {
    override val mokkeryContext = context
}
