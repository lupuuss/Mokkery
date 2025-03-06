package dev.mokkery

import dev.mokkery.internal.GlobalMokkeryScope
import dev.mokkery.internal.ObjectNotMockedException
import dev.mokkery.internal.context.resolveMockInstance
import dev.mokkery.internal.context.tools

/**
 * Removes all answers configured for given [mocks].
 */
public fun resetAnswers(vararg mocks: Any) {
    mocks.forEach {
        val instance = GlobalMokkeryScope.tools.resolveMockInstance(it) ?: throw ObjectNotMockedException(it)
        instance.mokkeryInterceptor.answering.reset()
    }
}

/**
 * Clears call history for all given [mocks].
 */
public fun resetCalls(vararg mocks: Any) {
    mocks.forEach {
        val instance = GlobalMokkeryScope.tools.resolveMockInstance(it) ?: throw ObjectNotMockedException(it)
        instance.mokkeryInterceptor.callTracing.reset()
    }
}
