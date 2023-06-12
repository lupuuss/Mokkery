@file:Suppress("RedundantSuspendModifier", "UNUSED_PARAMETER", "unused")

package dev.mokkery

import dev.mokkery.answer.MockAnswerScope
import dev.mokkery.answer.MockSuspendAnswerScope
import dev.mokkery.matcher.CallTemplateRegistry
import dev.mokkery.matcher.ArgMatchersScope
import dev.mokkery.tracking.CallTemplate

public fun <T> every(
    block: ArgMatchersScope.() -> T
): MockAnswerScope<T> = throw MokkeryPluginNotAppliedException()

public suspend fun <T> everySuspend(
    block: suspend ArgMatchersScope.() -> T
): MockSuspendAnswerScope<T> = throw MokkeryPluginNotAppliedException()

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
    val registry = CallTemplateRegistry()
    try {
        mock.mokkery.startTemplateRegistering(registry)
        block(ArgMatchersScope(registry))
    } finally {
        mock.mokkery.stopTemplateRegistering()
    }
    val template = registry.templates.singleOrNull() ?: throw NotSingleCallInEveryBlockException()
    return mock.mokkery to template
}
