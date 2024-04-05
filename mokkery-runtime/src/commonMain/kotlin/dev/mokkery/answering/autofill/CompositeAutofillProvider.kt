package dev.mokkery.answering.autofill

/**
 * Provider that allows registering both [types] and [delegates].
 * It uses given providers in specified order:
 * * [delegates]
 * * [types]
 */
public interface CompositeAutofillProvider : AutofillProvider<Any?> {

    /**
     * Allows injecting implementations of [AutofillProvider]. This provider is used first.
     */
    public val delegates: DelegateAutofillProvider<Any?>

    /**
     * Allows providing simple type-to-function mapping. This provider is used second.
     */
    public val types: TypeRegistryAutofillProvider
}
