package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.AutofillProvider.Value
import kotlin.reflect.KClass

internal actual object AnyValueProvider : AutofillProvider<Any?> {

    actual override fun provide(type: KClass<*>): Value<Any?> {
        val jClass = type.java
        return when {
            jClass.isAnnotationPresent(JvmInline::class.java) -> instantiateInlineClassOf(jClass)
            else -> null
        }.asAutofillProvided()
    }

    private fun instantiateInlineClassOf(jClass: Class<*>): Any = jClass.declaredConstructors
        .first()
        .let {
            val paramType = it.parameterTypes.first().kotlin
            it.isAccessible = true
            it.newInstance(buildInTypesMapping[paramType])
        }
}
