package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.AutofillProvider.Value
import dev.mokkery.answering.autofill.provideValue
import kotlin.test.Test
import kotlin.test.assertEquals

class ThreadSafeDelegateAutofillProviderTest {

    private val initial = AutofillProvider {
        when (it) {
            Int::class -> Value.Provided(0)
            String::class -> Value.Provided("")
            Byte::class -> Value.Provided<Byte>(0)
            else -> Value.Absent
        }
    }
    private val newProvider1 = AutofillProvider { if (it == Int::class) Value.Provided(37) else Value.Absent }
    private val newProvider2 = AutofillProvider { if (it == Double::class) Value.Provided(37.21) else Value.Absent }
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
        assertEquals(0 , provider.provideValue(Int::class))
    }

    @Test
    fun testReturnsAbsentWhenTheOnlyProviderUnregistered() {
        provider.unregister(newProvider2)
        assertEquals(Value.Absent , provider.provide(Double::class))
    }
}
