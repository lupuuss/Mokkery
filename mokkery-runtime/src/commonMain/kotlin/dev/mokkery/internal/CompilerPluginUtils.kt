@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.getIfProvided
import dev.mokkery.answering.autofill.provideValue
import dev.mokkery.internal.answering.autofill.AnyValueProvider
import dev.mokkery.internal.answering.autofill.asAutofillProvided
import kotlin.reflect.KClass

internal fun generateMockId(typeName: String) = MockUniqueReceiversGenerator.generate(typeName)

internal fun <T> autofillConstructor(type: KClass<*>): T = autofillConstructorProvider
    .provideValue(type)
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
