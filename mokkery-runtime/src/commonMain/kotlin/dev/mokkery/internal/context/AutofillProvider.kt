package dev.mokkery.internal.context

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.context.MokkeryContext

internal val MokkeryContext.autofillProvider: AutofillProvider<Any?>
    get() = get(ContextAutofillProvider)?.provider ?: error("AutofillProvider not present in the context!")

internal fun AutofillProvider<Any?>.asContext(): MokkeryContext = ContextAutofillProvider(this)

private class ContextAutofillProvider(
    val provider: AutofillProvider<Any?>
) : MokkeryContext.Element {

    override val key = Key

    companion object Key : MokkeryContext.Key<ContextAutofillProvider>
}
