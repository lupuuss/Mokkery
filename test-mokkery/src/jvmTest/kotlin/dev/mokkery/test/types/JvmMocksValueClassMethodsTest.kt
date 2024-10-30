package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.PrimitiveValueClass
import dev.mokkery.test.ValueClass
import dev.mokkery.test.ValueClassMethodsInterface
import org.junit.Test
import kotlin.test.assertEquals

class JvmMocksValueClassMethodsTest {

    private val mock = mock<ValueClassMethodsInterface<Int>>()

    @Test
    fun testCallValueClass() {
        every { mock.callValueClass(any()) } returns ValueClass(ComplexType)
        assertEquals<ValueClass<ComplexType>>(ValueClass(ComplexType), mock.callValueClass( ValueClass(ComplexType)))
    }

    @Test
    fun testMocksMethodsWithComplexResultReturnType() {
        every { mock.callPrimitiveValueClass(any()) } returns PrimitiveValueClass(1)
        assertEquals(PrimitiveValueClass(1), mock.callPrimitiveValueClass(PrimitiveValueClass(1)))
    }
}
