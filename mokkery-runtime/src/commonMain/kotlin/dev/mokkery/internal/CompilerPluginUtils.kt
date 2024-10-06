@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.getIfProvided
import dev.mokkery.answering.autofill.provideValue
import dev.mokkery.internal.answering.autofill.AnyValueProvider
import dev.mokkery.internal.answering.autofill.asAutofillProvided
import dev.mokkery.internal.templating.TemplatingScope
import kotlin.reflect.KClass

internal fun generateMockId(typeName: String) = MockUniqueReceiversGenerator.generate(typeName)

internal fun <T> autofillConstructor(type: KClass<*>): T = autofillConstructorProvider
    .provideValue(type.takeIfImplementedOrAny())
    .unsafeCast()

internal inline fun <reified T> callIgnoringClassCastException(templatingScope: Any?, block: () -> T): T {
    val initialTemplatesCount = templatingScope.templatesCount
    return try {
        block()
    } catch (e: ClassCastException) {
        autofillOrRethrow(T::class, e, initialTemplatesCount, templatingScope)
    }
}

internal fun <T> autofillOrRethrow(cls: KClass<*>, e: ClassCastException, initialTemplatesCount: Int, scope: Any?): T {
    if (initialTemplatesCount == scope.templatesCount) throw e
    return AutofillProvider
        .forInternals
        .provideValue(cls.takeIfImplementedOrAny())
        .unsafeCast()
}

internal val Any?.templatesCount: Int get() = (this as? TemplatingScope)?.templates?.size ?: 0

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
