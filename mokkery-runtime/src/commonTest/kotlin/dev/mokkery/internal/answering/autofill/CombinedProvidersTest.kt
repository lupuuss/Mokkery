package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.answering.autofill.AutofillProvider.Value
import dev.mokkery.answering.autofill.provideValue
import kotlin.test.Test
import kotlin.test.assertEquals

class CombinedProvidersTest {

    private val provider = CombinedProviders<Any?>(
        AutofillProvider.ofNotNull{ if (it == Int::class) 0 else null },
        AutofillProvider.ofNotNull{ if (it == String::class) "" else null },
    )

    private val newProvider = AutofillProvider.ofNotNull {
        when (it) {
            Int::class -> 37
            String::class -> "Hello!"
            Byte::class -> 5.toByte()
            else -> null
        }
    }

    @Test
    fun testReturnsFromFirstProviderWhenTypeIsHandledByThisProvider() {
        assertEquals(Value.Provided(0), provider.provide(Int::class))
    }

    @Test
    fun testReturnsFromSecondProviderWhenTypeIsHandledByThisProvider() {
        assertEquals(Value.Provided(""), provider.provide(String::class))
    }

    @Test
    fun testReturnsAbsentWhenTypeIsNotHandledByAnyProvider() {
        assertEquals(Value.Absent, provider.provide(Any::class))
    }

    @Test
    fun testWithFirstReturnsNewProvidedWithInjectedProviderAtFirstPosition() {
        val resultProvider = provider.withFirst(newProvider)
        assertEquals(37, resultProvider.provideValue(Int::class))
        assertEquals("Hello!", resultProvider.provideValue(String::class))
        assertEquals(5.toByte(), resultProvider.provideValue(Byte::class))
    }

    @Test
    fun testWithLastReturnsNewProvidedWithInjectedProviderAtLastPosition() {
        val resultProvider = provider.withLast(newProvider)
        assertEquals(0, resultProvider.provideValue(Int::class))
        assertEquals("", resultProvider.provideValue(String::class))
        assertEquals(5.toByte(), resultProvider.provideValue(Byte::class))
    }

    @Test
    fun testWithoutRemovesInjectedProvider() {
        val resultProvider = provider
            .withFirst(newProvider)
            .without(newProvider)
        assertEquals(0, resultProvider.provideValue(Int::class))
        assertEquals("", resultProvider.provideValue(String::class))
        assertEquals(Value.Absent, resultProvider.provide(Byte::class))
    }
}
