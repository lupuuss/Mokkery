package dev.mokkery.internal.context

import dev.mokkery.MokkeryScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.MokkeryInstance

internal val MokkeryScope.currentMokkeryInstance: MokkeryInstance
    get() = mokkeryContext.require(CurrentMokkeryInstance).value

internal class CurrentMokkeryInstance(val value: MokkeryInstance) : MokkeryContext.Element {

    override val key = Key

    companion object Key : MokkeryContext.Key<CurrentMokkeryInstance>
}
