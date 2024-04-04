package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.DelegateAutofillProvider
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlin.reflect.KClass

internal fun <T> threadSafeDelegateAutofillProvider(vararg initial: AutofillProvider<T>): DelegateAutofillProvider<T> {
    return ThreadSafeDelegateAutofillProvider(initial.toList())
}

private class ThreadSafeDelegateAutofillProvider<T>(initial: List<AutofillProvider<T>>) : DelegateAutofillProvider<T> {

    private val delegate = atomic(CombinedProviders(initial))
    
    override fun provide(type: KClass<*>): AutofillProvider.Value<T> = delegate.value.provide(type)

    override fun register(provider: AutofillProvider<T>) {
        delegate.update { it.withFirst(provider) }
    }

    override fun unregister(provider: AutofillProvider<T>) {
        delegate.update { it.without(provider) }
    }

}
