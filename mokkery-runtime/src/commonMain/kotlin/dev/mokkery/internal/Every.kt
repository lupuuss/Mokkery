@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.answering.UnifiedAnsweringScope
import dev.mokkery.internal.coroutines.runSuspension
import dev.mokkery.internal.templating.TemplatingScope
import dev.mokkery.matcher.ArgMatchersScope

internal fun <T> internalEverySuspend(
    scope: TemplatingScope,
    block: suspend ArgMatchersScope.() -> T
): SuspendAnsweringScope<T> = internalEvery(scope) { runSuspension { block() } }.unsafeCast()

internal fun <T> internalEvery(
    scope: TemplatingScope,
    block: ArgMatchersScope.() -> T
): BlockingAnsweringScope<T> {
    val result = runCatching { block(scope) }
    val exception = result.exceptionOrNull()
    if  (exception != null && exception !is DefaultNothingException) {
        scope.release()
        throw exception
    }
    return try {
        val template = scope.templates.singleOrNull() ?: throw NotSingleCallInEveryBlockException()
        val mock = scope.spies.first { it.id == template.receiver }
        UnifiedAnsweringScope(mock.interceptor.answering, template)
    } finally {
        scope.release()
    }
}
