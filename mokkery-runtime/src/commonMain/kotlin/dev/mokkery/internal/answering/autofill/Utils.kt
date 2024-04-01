package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider

internal fun <T> T.asAutofillProvided() = AutofillProvider.Value.Provided(this)
