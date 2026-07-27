package dev.mokkery.internal.context

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.interceptor.MokkeryInstantiationListener
import dev.mokkery.internal.requireInstanceScope

internal fun Any.invokeInstantiationListener() {
    val scope = requireInstanceScope()
    scope
        .instantiationListener
        .onInstantiation(scope, this)
}

internal val MokkeryInstanceScope.instantiationListener: MokkeryInstantiationListener
    get() = mokkeryContext.require(ContextInstantiationListener)

internal interface ContextInstantiationListener : MokkeryContext.Element, MokkeryInstantiationListener {

    override val key get() = Key

    companion object Key : MokkeryContext.Key<ContextInstantiationListener>
}

internal fun ContextInstantiationListener(
    vararg listeners: MokkeryInstantiationListener
): ContextInstantiationListener = object : ContextInstantiationListener {

    override fun onInstantiation(scope: MokkeryInstanceScope, mock: Any) = listeners.forEach {
        it.onInstantiation(scope, mock)
    }

    override fun toString(): String = "ContextInstantiationListener(listeners=$listeners)"
}
