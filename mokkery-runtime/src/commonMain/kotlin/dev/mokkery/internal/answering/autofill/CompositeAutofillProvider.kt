package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.CompositeAutofillProvider
import kotlin.reflect.KClass

internal fun <T> compositeAutofillProvider(vararg initial: AutofillProvider<T>): CompositeAutofillProvider<T> {
    return CompositeAutofillProviderImpl(initial = initial)
}

private class CompositeAutofillProviderImpl<T>(vararg initial: AutofillProvider<T>) : CompositeAutofillProvider<T> {

    override val types = threadSafeTypeRegistryAutofillProvider<T & Any>()

    override val delegates = threadSafeDelegateAutofillProvider(initial = initial).apply { register(types) }

    override fun provide(type: KClass<*>): AutofillProvider.Value<T> = delegates.provide(type)

}
