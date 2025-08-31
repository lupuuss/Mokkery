package dev.mokkery.internal.answering.autofill

import dev.mokkery.answering.autofill.AutofillProvider.Value
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class GenericArrayProviderTest {

    private val provider = GenericArrayProvider

    @Test
    fun testReturnsArrayWithSingleNullForGenericArrays() {
        val kClass = arrayOf("")::class
        val result = provider.provide(kClass)
        assertIs<Value.Provided<Array<String>>>(result)
        assertEquals(1, result.value.size)
        assertNull(result.value.getOrNull(0))
        assertEquals(kClass, result.value::class)
    }

    @Test
    @OptIn(ExperimentalUnsignedTypes::class)
    fun testReturnsNullForOtherArrays() {
        assertEquals(Value.Absent, provider.provide(IntArray::class))
        assertEquals(Value.Absent, provider.provide(UIntArray::class))
        assertEquals(Value.Absent, provider.provide(FloatArray::class))
    }

    @Test
    fun testReturnsNullForOtherTypes() {
        assertEquals(Value.Absent, provider.provide(Int::class))
        assertEquals(Value.Absent, provider.provide(Any::class))
        assertEquals(Value.Absent, provider.provide(Unit::class))
    }
}
