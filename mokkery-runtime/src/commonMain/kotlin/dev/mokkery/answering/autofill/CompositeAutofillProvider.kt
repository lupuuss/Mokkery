package dev.mokkery.answering.autofill

/**
 * Provider that allows registering both [types] and [delegates].
 * It uses given providers in specified order:
 * * [delegates]
 * * [types]
 */
public interface CompositeAutofillProvider<T> : AutofillProvider<T> {

    /**
     * Allows injecting implementations of [AutofillProvider]. This provider is used first.
     */
    public val delegates: DelegateAutofillProvider<T>

    /**
     * Allows providing simple type-to-function mapping. This provider is used second.
     */
    public val types: TypeRegistryAutofillProvider<T & Any>
}
