@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.RegularAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.answering.UnifiedAnsweringScope
import dev.mokkery.internal.answering.AnsweringInterceptor
import dev.mokkery.internal.coroutines.runSuspension
import dev.mokkery.internal.matcher.ArgMatchersScope
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.templating.TemplatingContext
import dev.mokkery.matcher.ArgMatchersScope

internal fun <T> internalEvery(
    context: TemplatingContext,
    block: ArgMatchersScope.() -> T
): RegularAnsweringScope<T> = internalBaseEvery(context) { block() }.let { UnifiedAnsweringScope(it.first, it.second) }

internal fun <T> internalEverySuspend(
    context: TemplatingContext,
    block: suspend ArgMatchersScope.() -> T
): SuspendAnsweringScope<T> = internalBaseEvery(context) { runSuspension { block() } }
    .let { UnifiedAnsweringScope(it.first, it.second) }

private inline fun <T> internalBaseEvery(
    context: TemplatingContext,
    block: ArgMatchersScope.() -> T
): Pair<AnsweringInterceptor, CallTemplate> {
    val result = runCatching { block(ArgMatchersScope(context)) }
    val exception = result.exceptionOrNull()
    if  (exception != null && exception !is DefaultNothingException) {
        context.release()
        throw exception
    }
    return try {
        val template = context.templates.singleOrNull() ?: throw NotSingleCallInEveryBlockException()
        val mock = context.spies.first { it.id == template.receiver }
        if (mock !is MokkeryMockScope) throw ObjectNotMockedException(mock)
        mock.interceptor.answering to template
    } finally {
        context.release()
    }
}
