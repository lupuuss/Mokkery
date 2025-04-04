package dev.mokkery

import dev.mokkery.internal.GlobalMokkeryScope
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.calls.callTracing
import dev.mokkery.internal.context.resolveScope
import dev.mokkery.internal.context.tools

/**
 * Removes all answers configured for given [mocks].
 */
public fun resetAnswers(vararg mocks: Any) {
    mocks.forEach {
        val instanceScope = GlobalMokkeryScope.tools.resolveScope(it)
        instanceScope.answering.reset()
    }
}

/**
 * Clears call history for all given [mocks].
 */
public fun resetCalls(vararg mocks: Any) {
    mocks.forEach {
        val instanceScope = GlobalMokkeryScope.tools.resolveScope(it)
        instanceScope.callTracing.reset()
    }
}
