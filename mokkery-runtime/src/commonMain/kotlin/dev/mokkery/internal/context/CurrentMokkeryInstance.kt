package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryInstance

internal class CurrentMokkeryInstance(val value: MokkeryInstance) : MokkeryContext.Element {

    override val key = Key

    companion object Key : MokkeryContext.Key<CurrentMokkeryInstance>
}
