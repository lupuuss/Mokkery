package dev.mokkery.answering.autofill

import kotlin.reflect.KClass

/**
 * Allows registering value providers for specific types. This provider does not support polymorphism. If
 * you register provider for some interface `A`, it provides a value only for `A::class`. Provider for `AImpl` must
 * be registered separately.
 */
public interface TypeRegistryAutofillProvider : AutofillProvider<Any?> {

    /**
     * Registers a [provider] for [type].
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
