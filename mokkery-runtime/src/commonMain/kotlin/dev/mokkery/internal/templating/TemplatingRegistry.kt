package dev.mokkery.internal.templating

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
        mock: Any,
        functionName: String,
        arguments: List<Pair<Function.Parameter, ArgMatcher<Any?>>>
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
        mock: Any,
        functionName: String,
        arguments: List<Pair<Function.Parameter, ArgMatcher<Any?>>>
    ) {
        val scope = mock.requireInstanceScope()
        _collection.upsertScope(scope)
        _templates.add(
            CallTemplate(
                instanceId = scope.instanceId,
                name = functionName,
                parameters = arguments.map { it.first },
                matchers = arguments.associate { it.first.name to it.second },
            )
        )
    }

    override fun toString(): String = "TemplatingRegistry(templates=${templates})"
}
