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

internal fun MokkeryScope.instanceFactoryScope(block: InstanceFactoryConfigurer.Block?): MokkeryScope {
    val context = mokkeryContext + forkedHooksOrEmpty()
    block ?: return MokkeryScope(context)
    return MokkeryScope(context.applyConfigurer(block))
}

internal fun MokkeryContext.applyConfigurer(block: InstanceFactoryConfigurer.Block): MokkeryContext {
    return applyConfigurer(::UnifiedInstanceFactoryConfigurer, block)
}

private class UnifiedInstanceFactoryConfigurer(
    context: MokkeryContext
) : BaseMokkeryConfigurer(context), MockFactoryConfigurer, SpyFactoryConfigurer
