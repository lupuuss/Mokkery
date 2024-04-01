package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.internal.DefaultNothingException
import kotlin.reflect.KClass

internal object NothingValueProvider : AutofillProvider<Nothing> {

    override fun provide(type: KClass<*>): AutofillProvider.Value.Absent = when (type) {
        Nothing::class -> throw DefaultNothingException()
        else -> AutofillProvider.Value.Absent
    }
}
