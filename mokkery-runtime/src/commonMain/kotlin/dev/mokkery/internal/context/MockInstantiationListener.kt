package dev.mokkery.internal.context

import dev.mokkery.context.forEach
import dev.mokkery.internal.MokkeryInstanceScope

internal fun MokkeryInstanceScope.invokeMockInstantiationListener(obj: Any) {
    mokkeryContext.forEach {
        if (it is MockInstantiationListener) it.onMockInstantiation(obj, this)
    }
}

internal fun interface MockInstantiationListener {

    fun onMockInstantiation(obj: Any, scope: MokkeryInstanceScope)
}
