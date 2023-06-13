@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answer.MockAnswerScope
import dev.mokkery.answer.MockSuspendAnswerScope
import dev.mokkery.internal.tracing.CallTemplate
import dev.mokkery.internal.tracing.CallTemplateTracer
import dev.mokkery.matcher.ArgMatchersScope

internal fun <T> internalEvery(
    mocks: Array<Any>,
    block: ArgMatchersScope.() -> T
): MockAnswerScope<T> = internalBaseEvery(mocks) { block() }.let { MockAnswerScope(it.first, it.second) }

internal suspend fun <T> internalEverySuspend(
    mocks: Array<Any>,
    block: suspend ArgMatchersScope.() -> T
): MockSuspendAnswerScope<T> = internalBaseEvery(mocks) { block() }.let { MockSuspendAnswerScope(it.first, it.second) }

private inline fun <T> internalBaseEvery(
    mocks: Array<Any>,
    block: ArgMatchersScope.() -> T
): Pair<Mokkery, CallTemplate> {
    val mock = mocks.singleOrNull() as? MokkeryScope ?: throw NotSingleCallInEveryBlockException()
    val registry = CallTemplateTracer()
    try {
        mock.mokkery.startTemplateRegistering(registry)
        block(ArgMatchersScope(registry))
    } finally {
        mock.mokkery.stopTemplateRegistering()
    }
    val template = registry.templates.singleOrNull() ?: throw NotSingleCallInEveryBlockException()
    return mock.mokkery to template
}
