package dev.mokkery.answering.autofill

/**
 * Provides values based on registered providers. Providers are utilized in order of their registration,
 * starting with the most recent.
 */
public interface DelegateAutofillProvider<T> : AutofillProvider<T> {

    /**
     * Registers [provider] to be used before previously registered providers.
     */
    public fun register(provider: AutofillProvider<T>)

    /**
     * Unregisters [provider] registered with [register] so it is no longer in use.
     */
    public fun unregister(provider: AutofillProvider<T>)
}
