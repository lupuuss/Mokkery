package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.isProvided
import kotlin.reflect.KClass

internal class CombinedProviders<T>(private vararg val providers: AutofillProvider<T>) : AutofillProvider<T?> {

    override fun provide(type: KClass<*>): AutofillProvider.Value<T> = providers
        .firstNotNullOfOrNull { value -> value.provide(type).takeIf(AutofillProvider.Value<T>::isProvided) }
        ?: AutofillProvider.Value.Absent
}
