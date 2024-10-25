package dev.mokkery.internal.context

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.context.MokkeryContext
import dev.mokkery.internal.MokkeryInstanceLookup

internal val GlobalMokkeryContext: MokkeryContext = createGlobalContext()

private fun createGlobalContext(): MokkeryContext {
    return MokkeryInstanceLookup() + MokkeryTools.default() + AutofillProvider.forInternals.asContext()
}
