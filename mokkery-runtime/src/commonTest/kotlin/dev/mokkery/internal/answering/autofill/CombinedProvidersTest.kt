package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider.Value
import kotlin.test.Test
import kotlin.test.assertEquals

class CombinedProvidersTest {

    private val provider = CombinedProviders<Any?>(
        { Value.providedIfNotNull { if (it == Int::class) 0 else null } },
        { Value.providedIfNotNull { if (it == String::class) "" else null } },
    )

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
}
