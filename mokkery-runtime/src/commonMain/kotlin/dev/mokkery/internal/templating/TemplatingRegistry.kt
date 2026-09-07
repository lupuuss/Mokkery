package dev.mokkery.internal.templating

import dev.mokkery.MokkeryInstanceScope
import dev.mokkery.context.Function
import dev.mokkery.context.MokkeryContext
import dev.mokkery.context.require
import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.MutableMokkeryCollection
import dev.mokkery.internal.instanceId
import dev.mokkery.internal.requireInstanceScope
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.templating.MokkeryTemplatingScope


internal interface TemplatingRegistry : MokkeryContext.Element {

    override val key: MokkeryContext.Key<*> get() = Key

    val collection: MokkeryCollection
    val templates: List<CallTemplate>

    fun register(
        scope: MokkeryInstanceScope,
        functionId: Function.Id,
        matchers: List<ArgMatcher<Any?>>
    )

    companion object Key : MokkeryContext.Key<TemplatingRegistry>
}

internal val MokkeryTemplatingScope.templatingRegistry: TemplatingRegistry
    get() = mokkeryContext.require(TemplatingRegistry)

internal val MokkeryTemplatingScope.registeredTemplates: List<CallTemplate>
    get() = templatingRegistry.templates

internal fun TemplatingRegistry(): TemplatingRegistry = TemplatingRegistryImpl()

private class TemplatingRegistryImpl : TemplatingRegistry {

    private val _templates = mutableListOf<CallTemplate>()
    private val _collection = MutableMokkeryCollection()
    override val collection: MokkeryCollection get() = _collection
    override val templates: List<CallTemplate> get() = _templates

    override fun register(
        scope: MokkeryInstanceScope,
        functionId: Function.Id,
        matchers: List<ArgMatcher<Any?>>
    ) {
        _collection.upsertScope(scope)
        _templates.add(
            CallTemplate(
                instanceId = scope.instanceId,
                functionId = functionId,
                matchers = matchers,
            )
        )
    }

    override fun toString(): String = "TemplatingRegistry(templates=${templates})"
}
