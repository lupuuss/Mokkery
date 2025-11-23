@file:Suppress("unused")

package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.getIfProvided
import dev.mokkery.answering.autofill.provideValue
import dev.mokkery.internal.utils.mokkeryRuntimeError
import dev.mokkery.internal.utils.takeIfImplementedOrAny
import dev.mokkery.internal.utils.unsafeCast
import kotlin.reflect.KClass

internal fun <T> autofillConstructor(type: KClass<*>): T = autofillConstructorProvider
    .provideValue(type.takeIfImplementedOrAny())
    .unsafeCast()

private val autofillConstructorProvider = AutofillProvider
    .forInternals
    .providingNotNullIfSupported()

private fun AutofillProvider<Any?>.providingNotNullIfSupported(): AutofillProvider<Any?> {
    val notNullProvider = AnyValueProvider.notNullIfSupported() ?: return this
    val original = this
    return object : AutofillProvider<Any> {
        override fun provide(type: KClass<*>): AutofillProvider.Value<Any> {
            val value = original.provide(type).getIfProvided()
                ?: notNullProvider.provide(type).getIfProvided()
                ?: mokkeryRuntimeError("Required instance of type $type, but internal machinery was not able to provide one!")
            return value.asAutofillProvided()
        }
    }
}
