package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import kotlin.reflect.KClass

internal expect object AnyValueProvider : AutofillProvider<Any?> {

    override fun provide(type: KClass<*>): AutofillProvider.Value<Any?>

    fun notNullIfSupported(): AutofillProvider<Any>?
}
