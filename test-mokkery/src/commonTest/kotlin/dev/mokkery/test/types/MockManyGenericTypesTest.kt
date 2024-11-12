package dev.mokkery.test.types

import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mockMany
import dev.mokkery.t1
import dev.mokkery.t2
import dev.mokkery.t3
import dev.mokkery.test.ComplexType
import kotlin.test.Test
import kotlin.test.assertEquals

class MockManyGenericTypesTest {


    private val mock = mockMany<A<String>, B<Int, String>, C<Double, Int, ComplexType>>()

    @Test
    fun testProperlyMocksAllMethods() {
        every { mock.t1.methodA(any()) } returnsArgAt 0
        every { mock.t2.methodB(any()) } calls { (i: Int) -> i.toString() }
        every { mock.t3.methodC(any(), any(), any()) } calls { (i1: Double, i2: Int, i3: ComplexType) ->
            ComplexType((i1.toInt() + i2 + i3.id.toInt()).toString())
        }
        assertEquals("Hello!", mock.t1.methodA("Hello!"))
        assertEquals("1", mock.t2.methodB(1))
        assertEquals(ComplexType("6"), mock.t3.methodC(1.0, 2, ComplexType("3")))
    }


    private interface A<T : Comparable<*>> {

        fun methodA(value: T): T
    }

    private interface B<in T : Number, out R : CharSequence> {

        fun methodB(value: T): R
    }

    private interface C<T1, T2, T3> {

        fun methodC(input1: T1, input2: T2, input3: T3): T3
    }
}
