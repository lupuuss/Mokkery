package dev.mokkery.internal.interceptor

import dev.mokkery.internal.MokkeryInstanceScope

internal interface MokkeryInstantiationListener {

    fun onInstantiation(scope: MokkeryInstanceScope, mock: Any)
}

@Suppress("unused")
internal fun MokkeryInstantiationListener(
    block: (scope: MokkeryInstanceScope, mock: Any) -> Unit
): MokkeryInstantiationListener {
    return object : MokkeryInstantiationListener {
        override fun onInstantiation(scope: MokkeryInstanceScope, mock: Any) = block(scope, mock)
    }
}
