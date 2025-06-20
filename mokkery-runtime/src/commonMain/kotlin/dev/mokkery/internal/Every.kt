@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.answering.UnifiedAnsweringScope
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.templating.TemplatingScopeImpl
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.templating.TemplatingScope

internal fun <T> internalEverySuspend(
    block: suspend TemplatingScope.() -> Unit
): SuspendAnsweringScope<T> = internalEvery<T> { runSuspension { block() } }.unsafeCast()

internal fun <T> internalEvery(
    block: TemplatingScope.() -> Unit
): BlockingAnsweringScope<T> {
    val scope = TemplatingScopeImpl()
    scope.apply(block)
    val template = scope.templates.singleOrNull() ?: throw NotSingleCallInEveryBlockException()
    val mock = scope.mocks.getScope(template.mockId)
    return UnifiedAnsweringScope(mock.answering, template)
}
