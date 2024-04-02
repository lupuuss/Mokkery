package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import kotlin.reflect.KClass

internal class TypeToFunctionAutofillProvider(private val values: Map<KClass<*>, () -> Any?>) : AutofillProvider<Any?> {

    override fun provide(type: KClass<*>): AutofillProvider.Value<Any?> = values[type]
        ?.let { AutofillProvider.Value.Provided(it()) }
        ?: AutofillProvider.Value.Absent
}
