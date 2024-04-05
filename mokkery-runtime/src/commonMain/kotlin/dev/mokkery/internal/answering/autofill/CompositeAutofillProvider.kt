package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.CompositeAutofillProvider
import kotlin.reflect.KClass

internal fun compositeAutofillProvider(vararg initial: AutofillProvider<Any?>): CompositeAutofillProvider {
    return CompositeAutofillProviderImpl(initial = initial)
}

private class CompositeAutofillProviderImpl(vararg initial: AutofillProvider<Any?>) : CompositeAutofillProvider {

    override val types = threadSafeTypeRegistryAutofillProvider()

    override val delegates = threadSafeDelegateAutofillProvider(initial = initial).apply { register(types) }

    override fun provide(type: KClass<*>): AutofillProvider.Value<Any?> = delegates.provide(type)

}
