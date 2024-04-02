package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider.Value
import dev.mokkery.answering.autofill.provideValue
import kotlin.test.Test
import kotlin.test.assertEquals

class TypeToFunctionAutofillProviderTest {

    private val provider = TypeToFunctionAutofillProvider(
        values = mapOf(
            Unit::class to {  },
            Int::class to { 0 },
            String::class to { "" },
            List::class to { null }
        )
    )

    @Test
    fun testReturnsValueForTypeProvidedOnCreation() {
        assertEquals(Unit, provider.provideValue(Unit::class))
        assertEquals(0, provider.provideValue(Int::class))
        assertEquals("", provider.provideValue(String::class))
    }

    @Test
    fun testReturnsNullProperly() {
        assertEquals(Value.Provided(null), provider.provide(List::class))
    }

    @Test
    fun testReturnsNullForOtherTypes() {
        assertEquals(Value.Absent, provider.provide(Byte::class))
        assertEquals(Value.Absent, provider.provide(Float::class))
        assertEquals(Value.Absent, provider.provide(Any::class))
    }
}
