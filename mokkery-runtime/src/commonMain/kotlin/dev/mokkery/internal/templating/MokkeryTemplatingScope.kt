package dev.mokkery.internal.templating

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.context.MokkeryInstancesRegistry
import dev.mokkery.internal.orEmpty
import dev.mokkery.internal.plus
import dev.mokkery.templating.MokkeryTemplatingScope
import kotlin.concurrent.Volatile

internal val MokkeryTemplatingScope.isClosed: Boolean
    get() = mokkeryContext[TemplatingRegistry] == null

internal fun MokkeryScope.templatingScope(block: MokkeryTemplatingScope.() -> Unit): MokkeryTemplatingScope {
    val context = mokkeryContext + TemplatingRegistry()
    val templatingScope = DetachableTemplatingScope(context)
    try {
        templatingScope.block()
    } finally {
        templatingScope.detach()
    }
    return MokkeryTemplatingScope(context)
}

internal fun MokkeryTemplatingScope(
    context: MokkeryContext
): MokkeryTemplatingScope = object : MokkeryTemplatingScope {
    override val mokkeryContext = context

    override fun toString(): String = "MokkeryTemplatingScope(mokkeryContext=$context)"
}

private class DetachableTemplatingScope(context: MokkeryContext) : MokkeryTemplatingScope {

    @Volatile
    override var mokkeryContext: MokkeryContext = context
        private set

    fun detach() {
        mokkeryContext = MokkeryContext.Empty
    }

    override fun toString(): String = "MokkeryTemplatingScope(mokkeryContext=$mokkeryContext)"
}

internal val MokkeryTemplatingScope.participatingInstances
    get() = mokkeryContext[MokkeryInstancesRegistry]
        ?.collection
        .orEmpty()
        .plus(templatingRegistry.collection)
