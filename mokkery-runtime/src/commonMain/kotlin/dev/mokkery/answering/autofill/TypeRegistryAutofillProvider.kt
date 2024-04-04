package dev.mokkery.answering.autofill

import kotlin.reflect.KClass

/**
 * Allows registering value providers for specific subtypes of [T]. This provider does not
 */
public interface TypeRegistryAutofillProvider<T : Any> : AutofillProvider<T> {

    /**
     * Registers a [provider] for [type] [R].
     * It overwrites any provider registered for the same [type] with this method.
     */
    public fun <R : T> register(type: KClass<R>, provider: () -> R)

    /**
     * Unregisters provider registered with [register] for [type] so it is no longer in use.
     */
    public fun unregister(type: KClass<*>)
}

/**
 * Calls [TypeRegistryAutofillProvider.register] with type [R].
 */
public inline fun <T : Any, reified R : T> TypeRegistryAutofillProvider<T>.register(noinline provider: () -> R) {
    register(R::class, provider)
}
