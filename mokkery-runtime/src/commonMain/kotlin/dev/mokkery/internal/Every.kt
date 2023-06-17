@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.RegularAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.answering.AnsweringInterceptor
import dev.mokkery.answering.UnifiedAnsweringScope
import dev.mokkery.internal.coroutines.runSuspension
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.templating.TemplatingContext
import dev.mokkery.matcher.ArgMatchersScope

internal fun <T> internalEvery(
    mocks: Array<Any>,
    block: ArgMatchersScope.() -> T
): RegularAnsweringScope<T> = internalBaseEvery(mocks) { block() }.let { UnifiedAnsweringScope(it.first, it.second) }

internal fun <T> internalEverySuspend(
    mocks: Array<Any>,
    block: suspend ArgMatchersScope.() -> T
): SuspendAnsweringScope<T> = internalBaseEvery(mocks) { runSuspension { block() } }
    .let { UnifiedAnsweringScope(it.first, it.second) }

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
