package dev.mokkery.test

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class DynamicTypesTest {

    private val mock = mock<DynamicInterface>()

    @Test
    fun testDynamicParameter() {
        every { mock.call(any()) } returns 1
        assertEquals(1, mock.call("value"))
        verify { mock.call("value") }
    }

    @Test
    fun testDynamicReturnType() {
        every { mock.produce() } returns "value"
        assertEquals("value", mock.produce())
    }

    @Test
    fun testDynamicTypeArgument() {
        every { mock.callList(any()) } returns 2
        assertEquals(2, mock.callList(listOf(1, "a")))
    }

    @Test
    fun testDynamicVararg() {
        every { mock.callVararg(1, any()) } returns 3
        assertEquals(3, mock.callVararg(1, "a"))
    }

    @Test
    fun testDynamicProperty() {
        every { mock.property } returns 4
        assertEquals(4, mock.property)
    }

    @Test
    fun testGenericMockWithDynamicTypeArgument() {
        val generic = mock<GenericDynamicInterface<dynamic>>()
        every { generic.call(any()) } returns 5
        assertEquals(5, generic.call("value"))
    }

    @Test
    fun testFunctionTypeWithDynamicParameter() {
        val function = mock<(dynamic) -> Int>()
        every { function(any()) } returns 6
        assertEquals(6, function("value"))
    }
}

interface DynamicInterface {
    fun call(value: dynamic): Int
    fun produce(): dynamic
    fun callList(values: List<dynamic>): Int
    fun callVararg(vararg values: dynamic): Int
    val property: dynamic
}

interface GenericDynamicInterface<T> {
    fun call(value: T): Int
}
