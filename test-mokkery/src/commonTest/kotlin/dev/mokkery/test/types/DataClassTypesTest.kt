package dev.mokkery.test.types

import dev.mokkery.MokkerySuiteScope
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.DataClass
import dev.mokkery.verify
import dev.mokkery.verifyNoMoreCalls
import kotlin.test.Test
import kotlin.test.assertEquals

class DataClassTypesTest {

    @Test
    fun testMocking() {
        val mock = mock<DataClass> {
            every { complexField1 } returns ComplexType
        }
        every { mock.primitiveField } returns 2
        assertEquals(ComplexType, mock.complexField1)
        assertEquals(2, mock.primitiveField)
    }

    @Test
    fun testVerifying() {
        val mock = mock<DataClass> {
            every { complexField1 } returns ComplexType
        }
        mock.complexField1
        verify {
            mock.complexField1
        }
    }

    @Test
    fun testScope() {
        val suite = object : MokkerySuiteScope {

            private val mock = mock<DataClass>()

            fun test() {
                every { mock.primitiveField } returns 1
                verifyNoMoreCalls()
            }
        }
        suite.test()
    }
}
