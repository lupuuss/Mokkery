@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.MokkeryScope
import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.MutablePropertyAnsweringScope
import dev.mokkery.answering.PropertyAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.annotations.Templating
import dev.mokkery.internal.answering.MutablePropertyAnsweringScopeImpl
import dev.mokkery.internal.answering.PropertyAnsweringScopeImpl
import dev.mokkery.internal.answering.UnifiedAnsweringScope
import dev.mokkery.internal.answering.answering
import dev.mokkery.internal.rendering.callTemplateRenderer
import dev.mokkery.internal.rendering.withRenderingScope
import dev.mokkery.internal.templating.templatingScope
import dev.mokkery.internal.templating.templatingRegistry
import dev.mokkery.internal.utils.runSuspension
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.templating.MokkeryTemplatingScope
import kotlin.reflect.KMutableProperty
import kotlin.reflect.KProperty

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

@PublishedApi
internal fun <T> MokkeryScope.internalEveryProperty(
    property: KProperty<T>,
    block: @Templating MokkeryTemplatingScope.() -> Unit
): PropertyAnsweringScope<T> {
    val scope = templatingScope()
    scope.apply(block)
    val registry = scope.templatingRegistry
    val template = registry.templates.singleOrNull() ?: scope.singleCallExpectedError()
    val instanceScope = registry.collection.getScope(template.instanceId)
    return PropertyAnsweringScopeImpl(instanceScope.answering, template, property)
}

@PublishedApi
internal fun <T> MokkeryScope.internalEveryMutableProperty(
    property: KMutableProperty<T>,
    templating: @Templating MokkeryTemplatingScope.() -> Unit,
    block: (MutablePropertyAnsweringScope<T>.() -> Unit)? = null,
): MutablePropertyAnsweringScope<T> {
    val scope = templatingScope()
    scope.apply(templating)
    val registry = scope.templatingRegistry
    val templates = registry.templates
    check(templates.size == 2) { "Expected getter and setter templates, but got ${templates.size}!" }
    val (getterTemplate, setterTemplate) = templates
    val instanceScope = registry.collection.getScope(getterTemplate.instanceId)
    val answeringScope = MutablePropertyAnsweringScopeImpl(
        answering = instanceScope.answering,
        getterTemplate = getterTemplate,
        setterTemplate = setterTemplate,
        property = property
    )
    block?.invoke(answeringScope)
    return answeringScope
}

private fun MokkeryTemplatingScope.singleCallExpectedError(): Nothing {
    val registry = templatingRegistry
    withRenderingScope {
        throw SingleCallInEveryBlockRequiredException(
            templates = registry.templates.map { callTemplateRenderer.render(it) }
        )
    }
}
