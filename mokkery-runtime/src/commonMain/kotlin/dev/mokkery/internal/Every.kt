@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.annotations.TemplatingLambda
import dev.mokkery.internal.answering.UnifiedAnsweringScope
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.templating.createTemplatingScope
import dev.mokkery.internal.templating.templatingRegistry
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.templating.MokkeryTemplatingScope

internal fun <T> internalEverySuspend(
    block: @TemplatingLambda suspend MokkeryTemplatingScope.() -> Unit
): SuspendAnsweringScope<T> = internalEvery<T> { runSuspension { block() } }.unsafeCast()

internal fun <T> internalEvery(
    block: @TemplatingLambda MokkeryTemplatingScope.() -> Unit
): BlockingAnsweringScope<T> {
    val scope = GlobalMokkeryScope.createTemplatingScope()
    scope.apply(block)
    val registry = scope.templatingRegistry
    val template = registry.templates.singleOrNull() ?: throw NotSingleCallInEveryBlockException()
    val instanceScope = registry.collection.getScope(template.instanceId)
    return UnifiedAnsweringScope(instanceScope.answering, template)
}
