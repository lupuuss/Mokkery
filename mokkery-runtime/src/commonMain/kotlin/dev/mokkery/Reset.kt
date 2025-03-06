package dev.mokkery

import dev.mokkery.internal.GlobalMokkeryScope
import dev.mokkery.internal.context.resolveScope
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.mokkeryMockInterceptor

/**
 * Removes all answers configured for given [mocks].
 */
public fun resetAnswers(vararg mocks: Any) {
    mocks.forEach {
        val instanceScope = GlobalMokkeryScope.tools.resolveScope(it)
        instanceScope.mokkeryMockInterceptor.answering.reset()
    }
}

/**
 * Clears call history for all given [mocks].
 */
public fun resetCalls(vararg mocks: Any) {
    mocks.forEach {
        val instanceScope = GlobalMokkeryScope.tools.resolveScope(it)
        instanceScope.mokkeryMockInterceptor.callTracing.reset()
    }
}
