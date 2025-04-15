package dev.mokkery

import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.calls.callTracing
import dev.mokkery.internal.requireInstanceScope

/**
 * Removes all answers configured for given [mocks].
 */
public fun resetAnswers(vararg mocks: Any) {
    mocks.forEach { it.requireInstanceScope().answering.reset() }
}

/**
 * Clears call history for all given [mocks].
 */
public fun resetCalls(vararg mocks: Any) {
    mocks.forEach { it.requireInstanceScope().callTracing.reset() }
}
