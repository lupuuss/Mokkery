package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider.Value
import kotlin.test.Test
import kotlin.test.assertEquals

class ValuesMapProviderTest {

    private val provider = ValuesMapProvider(
        values = mapOf(
            Unit::class to Unit,
            Int::class to 0,
            String::class to ""
        )
    )

    @Test
    fun testReturnsValueForTypeProvidedOnCreation() {
        assertEquals(Unit.asAutofillProvided(), provider.provide(Unit::class))
        assertEquals(0.asAutofillProvided(), provider.provide(Int::class))
        assertEquals("".asAutofillProvided(), provider.provide(String::class))
    }

    @Test
    fun testReturnsNullForOtherTypes() {
        assertEquals(Value.Absent, provider.provide(Byte::class))
        assertEquals(Value.Absent, provider.provide(Float::class))
        assertEquals(Value.Absent, provider.provide(Any::class))
    }
}
