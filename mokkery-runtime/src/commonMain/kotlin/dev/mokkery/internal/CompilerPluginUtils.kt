@file:Suppress("unused")

package dev.mokkery.internal

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.provideValue
import kotlin.reflect.KClass

internal fun generateMockId(typeName: String) = MockUniqueReceiversGenerator.generate(typeName)

internal fun <T> autofillConstructor(type: KClass<*>): T = autofillConstructorProvider
    .provideValue(type)
    .unsafeCast()

private val autofillConstructorProvider = AutofillProvider
    .forInternals
