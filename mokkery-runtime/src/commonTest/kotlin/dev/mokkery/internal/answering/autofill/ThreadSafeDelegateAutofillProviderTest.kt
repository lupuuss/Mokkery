package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.AutofillProvider.Value
import dev.mokkery.answering.autofill.provideValue
import kotlin.test.Test
import kotlin.test.assertEquals

class ThreadSafeDelegateAutofillProviderTest {

    private val initial = AutofillProvider.ofNotNull {
        when (it) {
            Int::class -> 0
            String::class -> ""
            Byte::class -> 0.toByte()
            else -> null
        }
    }
    private val newProvider1 = AutofillProvider.ofNotNull { if (it == Int::class) 37 else null }
    private val newProvider2 = AutofillProvider.ofNotNull { if (it == Double::class) 37.21 else null }
    private val provider = threadSafeDelegateAutofillProvider(initial)

    init {
        provider.register(newProvider1)
        provider.register(newProvider2)
    }

    @Test
    fun testReturnsValueFromRegisteredWhenItProvidesValueOfRequiredType() {
        assertEquals(37, provider.provideValue(Int::class))
        assertEquals(37.21, provider.provideValue(Double::class))
    }

    @Test
    fun testReturnsValueFromInitialWhenAbsentInRegistered() {
        assertEquals(0.toByte(), provider.provideValue(Byte::class))
    }

    @Test
    fun testReturnsFromRecentlyRegisteredProviderWhenItProvidesRequiredValue() {
        provider.register { if (it == Int::class) Value.Provided(21) else Value.Absent }
        assertEquals(21, provider.provideValue(Int::class))
    }

    @Test
    fun testReturnsFromInitialWhenLaterRegisteredProviderUnregistered() {
        provider.unregister(newProvider1)
        assertEquals(0, provider.provideValue(Int::class))
    }

    @Test
    fun testReturnsAbsentWhenTheOnlyProviderUnregistered() {
        provider.unregister(newProvider2)
        assertEquals(Value.Absent, provider.provide(Double::class))
    }
}
