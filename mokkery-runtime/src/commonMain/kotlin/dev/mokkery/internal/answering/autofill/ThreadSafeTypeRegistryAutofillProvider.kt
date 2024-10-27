package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.TypeRegistryAutofillProvider
import dev.mokkery.internal.utils.synchronizedMapOf
import kotlin.reflect.KClass

internal fun threadSafeTypeRegistryAutofillProvider(): TypeRegistryAutofillProvider {
    return ThreadSafeTypeRegistryAutofillProvider()
}

private class ThreadSafeTypeRegistryAutofillProvider : TypeRegistryAutofillProvider {

    private val registeredTypes = synchronizedMapOf<KClass<*>, () -> Any?>()
    private val internal = TypeToFunctionAutofillProvider(registeredTypes)

    override fun provide(type: KClass<*>): AutofillProvider.Value<Any?> = internal.provide(type)

    override fun <T> register(type: KClass<T & Any>, provider: () -> T?) {
        registeredTypes[type] = provider
    }

    override fun unregister(type: KClass<*>) {
        registeredTypes.remove(type)
    }
}
