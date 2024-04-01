package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.AutofillProvider.Value
import kotlin.reflect.KClass

internal actual object AnyValueProvider : AutofillProvider<Any?> {
    actual override fun provide(type: KClass<*>): Value<Any?> = object { }.asAutofillProvided()
}
