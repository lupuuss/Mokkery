package dev.mokkery

import dev.mokkery.internal.MokkeryMockScope
import dev.mokkery.internal.MokkerySpyScope
import dev.mokkery.internal.ObjectNotMockedException

/**
 * Removes all answers configured for given [mocks].
 */
public fun resetAnswers(vararg mocks: Any) {
    mocks.forEach {
        if (it !is MokkeryMockScope) throw ObjectNotMockedException(it)
        it.interceptor.answering.reset()
    }
}

/**
 * Clears call history for all given [mocks].
 */
public fun resetCalls(vararg mocks: Any) {
    mocks.forEach {
        if (it !is MokkerySpyScope) throw ObjectNotMockedException(it)
        it.interceptor.callTracing.reset()
    }
}
