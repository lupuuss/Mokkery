package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.TypeRegistryAutofillProvider
import dev.mokkery.internal.synchronizedMapOf
import dev.mokkery.internal.unsafeCast
import kotlin.reflect.KClass

internal fun <T : Any> threadSafeTypeRegistryAutofillProvider(): TypeRegistryAutofillProvider<T> {
    return ThreadSafeTypeRegistryAutofillProvider()
}

private class ThreadSafeTypeRegistryAutofillProvider<T : Any> : TypeRegistryAutofillProvider<T> {

    private val registeredTypes = synchronizedMapOf<KClass<*>, () -> Any>()
    private val internal = TypeToFunctionAutofillProvider(registeredTypes)

    override fun provide(type: KClass<*>): AutofillProvider.Value<T> = internal.provide(type).unsafeCast()

    override fun <R : T> register(type: KClass<R>, provider: () -> R) {
        registeredTypes[type] = provider
    }

    override fun unregister(type: KClass<*>) {
        registeredTypes.remove(type)
    }
}
