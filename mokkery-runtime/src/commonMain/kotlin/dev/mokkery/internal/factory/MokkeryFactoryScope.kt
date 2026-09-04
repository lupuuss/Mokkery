@file:Suppress("unused")

package dev.mokkery.internal.factory

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.factory.configurer.InstanceFactoryConfigurer
import dev.mokkery.factory.configurer.MockFactoryConfigurer
import dev.mokkery.factory.configurer.SpyFactoryConfigurer
import dev.mokkery.internal.configurer.BaseMokkeryConfigurer
import dev.mokkery.internal.configurer.applyConfigurer
import dev.mokkery.internal.interceptor.forkedHooksOrEmpty
import dev.mokkery.internal.presets.MokkeryInstancePresets

@PublishedApi
internal fun MokkeryScope.instanceFactoryScope(block: InstanceFactoryConfigurer.Block?): MokkeryScope {
    // in case of the factory copy we want to preserve the presets, but not share them with the original factory
    val presets = mokkeryContext[MokkeryInstancePresets]?.copy() ?: MokkeryInstancePresets()
    val context = mokkeryContext + forkedHooksOrEmpty() + presets
    val configured = if (block == null) context else context.applyConfigurer(block)
    return MokkeryScope(if (presets.isEmpty) configured - MokkeryInstancePresets else configured)
}

internal fun MokkeryContext.applyConfigurer(block: InstanceFactoryConfigurer.Block): MokkeryContext {
    return applyConfigurer(::UnifiedInstanceFactoryConfigurer, block)
}

private class UnifiedInstanceFactoryConfigurer(
    context: MokkeryContext
) : BaseMokkeryConfigurer(context), MockFactoryConfigurer, SpyFactoryConfigurer
