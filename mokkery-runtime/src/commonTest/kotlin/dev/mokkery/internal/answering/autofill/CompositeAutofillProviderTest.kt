package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.AutofillProvider.Value
import dev.mokkery.answering.autofill.provideValue
import dev.mokkery.answering.autofill.register
import kotlin.test.Test
import kotlin.test.assertEquals

class CompositeAutofillProviderTest {

    private val initial = AutofillProvider.ofNotNull {
        when (it) {
            Int::class -> 0
            String::class -> ""
            Byte::class -> 0.toByte()
            else -> null
        }
    }
    private val provider = compositeAutofillProvider(initial)

    init {
        val newProvider = AutofillProvider { if (it == Int::class) Value.Provided(37) else Value.Absent }
        provider.delegates.register(newProvider)
        provider.types.register { 13 }
        provider.types.register { "Hello!" }
    }

    @Test
    fun testUsesRegisteredDelegatesFirst() {
        assertEquals(37, provider.provideValue(Int::class))
    }

    @Test
    fun testUsesTypesWhenDelegatesProvideAbsent() {
        assertEquals("Hello!", provider.provideValue(String::class))
    }


    @Test
    fun testUsesInitialProviderWhenTypesProvideAbsent() {
        assertEquals(0.toByte(), provider.provideValue(Byte::class))
    }
}
