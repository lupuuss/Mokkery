package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryInstance
import dev.mokkery.internal.dynamic.MokkeryInstanceLookup

internal val MokkeryContext.self: Any?
    get() = get(CurrentMokkeryInstance)
        ?.value
        ?.let(MokkeryInstanceLookup.current::reverseResolve)
        ?: error("`self` not found in the context!")

internal val MokkeryContext.currentInstance: MokkeryInstance
    get() = get(CurrentMokkeryInstance)
        ?.value
        ?: error("Mokkery instance not found in the context!")

internal class CurrentMokkeryInstance(val value: MokkeryInstance) : MokkeryContext.Element {

    override val key = Key

    companion object Key : MokkeryContext.Key<CurrentMokkeryInstance>
}
