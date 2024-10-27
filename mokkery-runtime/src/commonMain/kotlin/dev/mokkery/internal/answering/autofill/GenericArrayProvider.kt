package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.internal.utils.isArray
import dev.mokkery.internal.utils.platformArrayOf
import kotlin.reflect.KClass

internal object GenericArrayProvider : AutofillProvider<Array<*>> {

    override fun provide(type: KClass<*>): AutofillProvider.Value<Array<*>> = when {
        type.isArray() -> platformArrayOf(type, listOf(null)).asAutofillProvided()
        else -> AutofillProvider.Value.Absent
    }
}
