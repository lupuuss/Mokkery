package dev.mokkery

import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.requireInstanceScope
import dev.mokkery.internal.toMokkeryCollection
import dev.mokkery.internal.tracing.withTracingSession

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
    mocks
        .map { it.requireInstanceScope() }
        .toMokkeryCollection()
        .withTracingSession { resetAll() }
}
