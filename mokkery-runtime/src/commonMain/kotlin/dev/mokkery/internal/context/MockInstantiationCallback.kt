package dev.mokkery.internal.context

import dev.mokkery.MokkeryScope
import dev.mokkery.context.forEach

internal fun MokkeryScope.invokeMockInstantiationCallbacks() {
    mokkeryContext.forEach {
        if (it is MockInstantiationListener) it.onMockInstantiation(this)
    }
}

internal fun interface MockInstantiationListener {

    fun onMockInstantiation(scope: MokkeryScope)
}
