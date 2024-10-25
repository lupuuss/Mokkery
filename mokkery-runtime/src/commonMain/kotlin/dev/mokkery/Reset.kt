package dev.mokkery

import dev.mokkery.internal.MokkeryMockInstance
import dev.mokkery.internal.ObjectNotMockedException
import dev.mokkery.internal.context.GlobalMokkeryContext
import dev.mokkery.internal.mokkeryInstanceLookup
import dev.mokkery.internal.interceptor

/**
 * Removes all answers configured for given [mocks].
 */
public fun resetAnswers(vararg mocks: Any) {
    mocks.forEach {
        val instance = GlobalMokkeryContext.mokkeryInstanceLookup.resolve(it)
        if (instance !is MokkeryMockInstance) throw ObjectNotMockedException(it)
        instance.interceptor.answering.reset()
    }
}

/**
 * Clears call history for all given [mocks].
 */
public fun resetCalls(vararg mocks: Any) {
    mocks.forEach {
        val instance = GlobalMokkeryContext.mokkeryInstanceLookup.resolve(it)
        if (instance !is MokkeryMockInstance) throw ObjectNotMockedException(it)
        instance.interceptor.callTracing.reset()
    }
}
