package dev.mokkery.internal.calls

import dev.mokkery.internal.MokkeryInstanceScope
import dev.mokkery.internal.mokkeryScope

internal interface TemplatingScopeDataBinder {

    fun getDataFor(token: Int): RawTemplateData?

    fun bind(token: Int, obj: Any?): MokkeryInstanceScope?

    fun firstProperlyBoundedData(): RawTemplateData

    fun reset()
}


internal fun TemplatingScopeDataBinder(): TemplatingScopeDataBinder = TemplatingScopeDataBinderImpl()

private class TemplatingScopeDataBinderImpl() : TemplatingScopeDataBinder {

    private val data = mutableMapOf<Int, RawTemplateData>()
    private val bindings = mutableMapOf<Int, Any>()

    override fun getDataFor(token: Int): RawTemplateData? {
        val binding = bindings[token]
        if (binding != null && binding.mokkeryScope == null) return null
        return data.getOrPut(token) { RawTemplateData() }
    }

    override fun bind(token: Int, obj: Any?): MokkeryInstanceScope? {
        obj ?: return null
        bindings[token] = obj
        val scope = obj.mokkeryScope
        if (scope != null) getDataFor(token)
        return scope
    }

    override fun firstProperlyBoundedData(): RawTemplateData = bindings.keys.firstNotNullOf(::getDataFor)

    override fun reset() {
        data.clear()
        bindings.clear()
    }
}
