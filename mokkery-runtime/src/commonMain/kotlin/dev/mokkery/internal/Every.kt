@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.answering.UnifiedAnsweringScope
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.internal.calls.TemplatingScope
import dev.mokkery.internal.utils.getScope
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
        val mock = scope.mocks.getScope(template.receiver)
        UnifiedAnsweringScope(mock.answering, template)
    } finally {
        scope.release()
    }
}
