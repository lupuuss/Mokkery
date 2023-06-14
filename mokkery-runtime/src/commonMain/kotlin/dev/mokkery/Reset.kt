package dev.mokkery

import dev.mokkery.internal.MokkeryMockScope
import dev.mokkery.internal.MokkerySpyScope
import dev.mokkery.internal.ObjectNotMockedException

public fun resetAnswers(vararg mocks: Any) {
    mocks.forEach {
        if (it !is MokkeryMockScope) throw ObjectNotMockedException(it)
        it.interceptor.answering.reset()
    }
}

public fun resetCalls(vararg mocks: Any) {
    mocks.forEach {
        if (it !is MokkerySpyScope) throw ObjectNotMockedException(it)
        it.interceptor.callTracing.reset()
    }
}
