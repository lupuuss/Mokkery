@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkeryScope
import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.annotations.Templating
import dev.mokkery.internal.answering.UnifiedAnsweringScope
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.rendering.callTemplateRenderer
import dev.mokkery.internal.rendering.withRenderingScope
import dev.mokkery.internal.templating.templatingScope
import dev.mokkery.internal.templating.templatingRegistry
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.templating.MokkeryTemplatingScope

@PublishedApi
internal fun <T> MokkeryScope.internalEverySuspend(
    block: @Templating suspend MokkeryTemplatingScope.() -> Unit
): SuspendAnsweringScope<T> = internalEvery<T> { runSuspension { block() } }.unsafeCast()

@PublishedApi
internal fun <T> MokkeryScope.internalEvery(
    block: @Templating MokkeryTemplatingScope.() -> Unit
): BlockingAnsweringScope<T> {
    val scope = templatingScope()
    scope.apply(block)
    val registry = scope.templatingRegistry
    val template = registry.templates.singleOrNull() ?: scope.singleCallExpectedError()
    val instanceScope = registry.collection.getScope(template.instanceId)
    return UnifiedAnsweringScope(instanceScope.answering, template)
}

private fun MokkeryTemplatingScope.singleCallExpectedError(): Nothing {
    val registry = templatingRegistry
    withRenderingScope {
        throw SingleCallInEveryBlockRequiredException(
            templates = registry.templates.map { callTemplateRenderer.render(it) }
        )
    }
}
