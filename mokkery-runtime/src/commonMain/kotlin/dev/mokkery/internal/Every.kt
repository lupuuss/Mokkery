@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.RegularAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.answering.UnifiedAnsweringScope
import dev.mokkery.internal.answering.AnsweringInterceptor
import dev.mokkery.internal.coroutines.runSuspension
import dev.mokkery.internal.matcher.ArgMatchersScope
import dev.mokkery.internal.templating.CallTemplate
import dev.mokkery.internal.templating.TemplatingScope
import dev.mokkery.matcher.ArgMatchersScope

internal fun <T> internalEvery(
    scope: TemplatingScope,
    block: ArgMatchersScope.() -> T
): RegularAnsweringScope<T> = internalBaseEvery(scope) { block() }.let { UnifiedAnsweringScope(it.first, it.second) }

internal fun <T> internalEverySuspend(
    scope: TemplatingScope,
    block: suspend ArgMatchersScope.() -> T
): SuspendAnsweringScope<T> = internalBaseEvery(scope) { runSuspension { block() } }
    .let { UnifiedAnsweringScope(it.first, it.second) }

private inline fun <T> internalBaseEvery(
    scope: TemplatingScope,
    block: ArgMatchersScope.() -> T
): Pair<AnsweringInterceptor, CallTemplate> {
    val result = runCatching { block(ArgMatchersScope(scope)) }
    val exception = result.exceptionOrNull()
    if  (exception != null && exception !is DefaultNothingException) {
        scope.release()
        throw exception
    }
    return try {
        val template = scope.templates.singleOrNull() ?: throw NotSingleCallInEveryBlockException()
        val mock = scope.spies.first { it.id == template.receiver }
        if (mock !is MokkeryMockScope) throw ObjectNotMockedException(mock)
        mock.interceptor.answering to template
    } finally {
        scope.release()
    }
}
