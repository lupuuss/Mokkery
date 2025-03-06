package dev.mokkery.internal.calls

import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.MokkeryScopeLookup

internal interface TemplatingScopeDataBinder {

    fun getDataFor(token: Int): RawTemplateData?

    fun bind(token: Int, obj: Any?): MokkeryInstanceScope?

    fun firstProperlyBoundedData(): RawTemplateData

    fun reset()
}


internal fun TemplatingScopeDataBinder(
    lookup: MokkeryScopeLookup
): TemplatingScopeDataBinder = TemplatingScopeDataBinderImpl(lookup)

private class TemplatingScopeDataBinderImpl(
    private val lookup: MokkeryScopeLookup
) : TemplatingScopeDataBinder {

    private val data = mutableMapOf<Int, RawTemplateData>()
    private val bindings = mutableMapOf<Int, Any>()

    override fun getDataFor(token: Int): RawTemplateData? {
        val binding = bindings[token]
        if (binding != null && lookup.resolveScopeOrNull(binding) == null) return null
        return data.getOrPut(token) { RawTemplateData() }
    }

    override fun bind(token: Int, obj: Any?): MokkeryInstanceScope? {
        obj ?: return null
        bindings[token] = obj
        val scope = lookup.resolveScopeOrNull(obj)
        if (scope != null) getDataFor(token)
        return scope
    }

    override fun firstProperlyBoundedData(): RawTemplateData = bindings.keys.firstNotNullOf(::getDataFor)

    override fun reset() {
        data.clear()
        bindings.clear()
    }
}
