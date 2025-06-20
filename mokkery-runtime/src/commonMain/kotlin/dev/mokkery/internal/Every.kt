@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.answering.UnifiedAnsweringScope
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.templating.TemplatingScope
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.internal.utils.getScope
import dev.mokkery.internal.utils.unsafeCast

internal fun <T> internalEverySuspend(
    block: suspend TemplatingScope.() -> Unit
): SuspendAnsweringScope<T> = internalEvery<T> { runSuspension { block() } }.unsafeCast()

internal fun <T> internalEvery(
    block: TemplatingScope.() -> Unit
): BlockingAnsweringScope<T> {
    val scope = TemplatingScope()
    scope.apply(block)
    val template = scope.templates.singleOrNull() ?: throw NotSingleCallInEveryBlockException()
    val mock = scope.mocks.getScope(template.mockId)
    return UnifiedAnsweringScope(mock.answering, template)
}
