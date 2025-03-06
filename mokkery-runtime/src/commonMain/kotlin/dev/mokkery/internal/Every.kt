@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.answering.UnifiedAnsweringScope
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.internal.calls.TemplatingScope
import dev.mokkery.internal.utils.first
import dev.mokkery.internal.utils.getValue
import dev.mokkery.internal.utils.unsafeCast
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
        val mock = scope.mocks.getValue(template.receiver)
        UnifiedAnsweringScope(mock.mokkeryMockInterceptor.answering, template)
    } finally {
        scope.release()
    }
}
