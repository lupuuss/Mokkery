package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider.Value
import dev.mokkery.internal.DefaultNothingException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NothingValueProviderTest {

    private val provider = NothingValueProvider

    @Test
    fun testFailsWithDefaultNothingExceptionWhenTypeIsNothing() {
        assertFailsWith<DefaultNothingException> { provider.provide(Nothing::class) }
    }

    @Test
    fun testReturnsNullForOtherTypes() {
        assertEquals(Value.Absent, provider.provide(Int::class))
        assertEquals(Value.Absent, provider.provide(Unit::class))
        assertEquals(Value.Absent, provider.provide(Any::class))
    }
}
