package dev.mokkery.answering.autofill

import kotlin.reflect.KClass

/**
 * Allows registering value providers for specific subtypes of [T]. This provider does not
 */
public interface TypeRegistryAutofillProvider : AutofillProvider<Any?> {

    /**
     * Registers a [provider] for [type] [R].
     * It overwrites any provider registered for the same [type] with this method.
     */
    public fun <T> register(type: KClass<T & Any>, provider: () -> T?)

    /**
     * Unregisters provider registered with [register] for [type] so it is no longer in use.
     */
    public fun unregister(type: KClass<*>)
}

/**
 * Calls [TypeRegistryAutofillProvider.register] with type [T].
 */
public inline fun <reified T : Any> TypeRegistryAutofillProvider.register(noinline provider: () -> T?) {
    register(T::class, provider)
}

/**
 * Calls [TypeRegistryAutofillProvider.unregister] with type [T].
 */
public inline fun <reified T : Any> TypeRegistryAutofillProvider.unregister() {
    unregister(T::class)
}
