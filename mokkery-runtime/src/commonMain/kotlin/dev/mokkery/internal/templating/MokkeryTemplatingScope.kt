package dev.mokkery.internal.templating

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.context.MokkeryInstancesRegistry
import dev.mokkery.internal.orEmpty
import dev.mokkery.internal.plus
import dev.mokkery.templating.MokkeryTemplatingScope

internal fun MokkeryScope.createTemplatingScope(): MokkeryTemplatingScope {
    return MokkeryTemplatingScope(mokkeryContext + TemplatingRegistry())
}

internal fun MokkeryTemplatingScope(
    context: MokkeryContext
): MokkeryTemplatingScope = object : MokkeryTemplatingScope {
    override val mokkeryContext = context

    override fun toString(): String = "MokkeryTemplatingScope(mokkeryContext=$context)"
}

internal val MokkeryTemplatingScope.participatingInstances
    get() = mokkeryContext[MokkeryInstancesRegistry]
        ?.collection
        .orEmpty()
        .plus(templatingRegistry.collection)
