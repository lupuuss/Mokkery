package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.provideValue
import kotlin.test.Test
import kotlin.test.assertEquals


class ThreadSafeTypeRegistryAutofillProviderTest {

    private val provider = threadSafeTypeRegistryAutofillProvider<Any>()

    init {
        provider.register(Int::class) { 37 }
        provider.register(String::class) { "Hello!" }
    }

    @Test
    fun testReturnsRegisteredValues() {
        assertEquals(37, provider.provideValue(Int::class))
        assertEquals("Hello!", provider.provideValue(String::class))
    }

    @Test
    fun testReturnsAbsentWhenNotRegistered() {
        assertEquals(AutofillProvider.Value.Absent, provider.provide(Any::class))
    }

    @Test
    fun testReturnsAbsentWhenUnregistered() {
        provider.unregister(Int::class)
        assertEquals(AutofillProvider.Value.Absent, provider.provide(Int::class))
    }

    @Test
    fun testReturnsFromNewProviderWhenOverwritten() {
        provider.register(Int::class) { 21 }
        assertEquals(21, provider.provideValue(Int::class))
    }
}
