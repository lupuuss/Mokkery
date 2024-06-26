package dev.mokkery.internal.templating

import dev.mokkery.internal.MokkeryMockScope
import dev.mokkery.internal.dynamic.MokkeryScopeLookup

internal interface TemplatingScopeDataBinder {

    fun getDataFor(token: Int): RawTemplateData?

    fun bind(token: Int, obj: Any?): MokkeryMockScope?

    fun firstProperlyBoundedData(): RawTemplateData

    fun reset()
}


internal fun TemplatingScopeDataBinder(
    lookup: MokkeryScopeLookup = MokkeryScopeLookup.current,
): TemplatingScopeDataBinder = TemplatingScopeDataBinderImpl(lookup)

private class TemplatingScopeDataBinderImpl(
    private val lookup: MokkeryScopeLookup
) : TemplatingScopeDataBinder {

    private val data = mutableMapOf<Int, RawTemplateData>()
    private val bindings = mutableMapOf<Int, Any>()

    override fun getDataFor(token: Int): RawTemplateData? {
        val binding = bindings[token]
        if (binding != null && lookup.resolve(binding) == null) return null
        return data.getOrPut(token) { RawTemplateData() }
    }

    override fun bind(token: Int, obj: Any?): MokkeryMockScope? {
        obj ?: return null
        bindings[token] = obj
        val scope = lookup.resolve(obj) as? MokkeryMockScope
        if (scope != null) getDataFor(token)
        return scope
    }

    override fun firstProperlyBoundedData(): RawTemplateData = bindings.keys.firstNotNullOf(::getDataFor)

    override fun reset() {
        data.clear()
        bindings.clear()
    }
}
