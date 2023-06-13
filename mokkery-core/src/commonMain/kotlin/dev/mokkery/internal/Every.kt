@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answer.MockAnswerScope
import dev.mokkery.answer.MockSuspendAnswerScope
import dev.mokkery.internal.answer.AnsweringInterceptor
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.templating.TemplatingContext
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
): Pair<AnsweringInterceptor, CallTemplate> {
    val mockScope = mocks.singleOrNull() ?: throw NotSingleCallInEveryBlockException()
    if (mockScope !is MokkeryMockScope) throw ObjectNotMockedException(mockScope)
    val mock = mockScope.interceptor
    val context = TemplatingContext()
    try {
        mock.templating.start(context)
        block(ArgMatchersScope(context))
    } finally {
        mock.templating.stop()
    }
    val template = context.templates.singleOrNull() ?: throw NotSingleCallInEveryBlockException()
    return mock.answering to template
}
