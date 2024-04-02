package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.isProvided
import kotlin.reflect.KClass

internal class CombinedProviders<T>(providers: List<AutofillProvider<T>>) : AutofillProvider<T> {

    private val providers = providers.distinct()

    constructor(vararg providers: AutofillProvider<T>) : this(providers.toList())

    override fun provide(type: KClass<*>): AutofillProvider.Value<T> = providers
        .firstNotNullOfOrNull { value -> value.provide(type).takeIf(AutofillProvider.Value<T>::isProvided) }
        ?: AutofillProvider.Value.Absent

    fun withFirst(provider: AutofillProvider<T>) = CombinedProviders(listOf(provider) + providers)

    fun withLast(provider: AutofillProvider<T>) = CombinedProviders(providers + provider)

    fun without(provider: AutofillProvider<T>) = CombinedProviders(providers - provider)
}
