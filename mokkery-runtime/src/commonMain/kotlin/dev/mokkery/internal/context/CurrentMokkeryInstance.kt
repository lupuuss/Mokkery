package dev.mokkery.internal.context

import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.internal.MokkeryInstance

internal val MokkeryCallScope.currentMokkeryInstance: MokkeryInstance
    get() = context.require(CurrentMokkeryInstance).value

internal class CurrentMokkeryInstance(val value: MokkeryInstance) : MokkeryContext.Element {

    override val key = Key

    companion object Key : MokkeryContext.Key<CurrentMokkeryInstance>
}
