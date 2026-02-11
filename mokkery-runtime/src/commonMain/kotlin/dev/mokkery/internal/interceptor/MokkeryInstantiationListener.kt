package dev.mokkery.internal.interceptor

import dev.mokkery.MokkeryInstanceScope

internal fun interface MokkeryInstantiationListener {

    fun onInstantiation(scope: MokkeryInstanceScope, mock: Any)
}
