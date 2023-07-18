package dev.mokkery

import dev.mokkery.internal.MokkeryMockScope
import dev.mokkery.internal.MokkerySpyScope
import dev.mokkery.internal.ObjectNotMockedException
import dev.mokkery.internal.dynamic.MokkeryScopeLookup

/**
 * Removes all answers configured for given [mocks].
 */
public fun resetAnswers(vararg mocks: Any) {
    mocks.forEach {
        val scope = MokkeryScopeLookup.current.resolve(it)
        if (scope !is MokkeryMockScope) throw ObjectNotMockedException(it)
        scope.interceptor.answering.reset()
    }
}

/**
 * Clears call history for all given [mocks].
 */
public fun resetCalls(vararg mocks: Any) {
    mocks.forEach {
        val scope = MokkeryScopeLookup.current.resolve(it)
        if (scope !is MokkerySpyScope) throw ObjectNotMockedException(it)
        scope.interceptor.callTracing.reset()
    }
}
