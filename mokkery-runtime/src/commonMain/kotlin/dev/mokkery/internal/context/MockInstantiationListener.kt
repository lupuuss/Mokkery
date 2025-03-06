package dev.mokkery.internal.context

import dev.mokkery.context.forEach
import dev.mokkery.internal.MokkeryInstanceScope

internal fun MokkeryInstanceScope.invokeMockInstantiationCallbacks() {
    mokkeryContext.forEach {
        if (it is MockInstantiationListener) it.onMockInstantiation(this)
    }
}

internal fun interface MockInstantiationListener {

    fun onMockInstantiation(scope: MokkeryInstanceScope)
}
