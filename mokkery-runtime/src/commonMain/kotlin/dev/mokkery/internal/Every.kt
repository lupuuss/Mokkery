@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkeryScope
import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.annotations.Templating
import dev.mokkery.internal.answering.UnifiedAnsweringScope
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.context.tools
import dev.mokkery.internal.names.withShorterNames
import dev.mokkery.internal.render.callTemplate
import dev.mokkery.internal.templating.createTemplatingScope
import dev.mokkery.internal.templating.templatingRegistry
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.templating.MokkeryTemplatingScope

internal fun <T> internalEverySuspend(
    block: @Templating suspend MokkeryTemplatingScope.() -> Unit
): SuspendAnsweringScope<T> = internalEvery<T> { runSuspension { block() } }.unsafeCast()

internal fun <T> internalEvery(
    block: @Templating MokkeryTemplatingScope.() -> Unit
): BlockingAnsweringScope<T> {
    val scope = MokkeryScope.global.createTemplatingScope()
    scope.apply(block)
    val registry = scope.templatingRegistry
    val template = registry.templates.singleOrNull() ?: scope.singleCallExpectedError()
    val instanceScope = registry.collection.getScope(template.instanceId)
    return UnifiedAnsweringScope(instanceScope.answering, template)
}

private fun MokkeryTemplatingScope.singleCallExpectedError(): Nothing {
    val registry = templatingRegistry
    val aliases = registry
        .collection
        .withShorterNames(tools.namesShortener)
    val renderer = tools
        .renderers
        .callTemplate(aliases = aliases)
    throw SingleCallInEveryBlockRequiredException(templates = registry.templates.map(renderer::render),)
}
