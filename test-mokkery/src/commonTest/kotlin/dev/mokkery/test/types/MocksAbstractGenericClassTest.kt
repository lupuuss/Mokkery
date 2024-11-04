package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.AbstractGenericClass
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksAbstractGenericClassTest {

    private val mock = mock<AbstractGenericClass<CharSequence>>()

    @Test
    fun testCall() {
        every { mock.call(any()) } returns "Hello!"
        assertEquals("Hello!", mock.call(""))
        verify { mock.call(any()) }
    }

    @Test
    fun testCallGeneric() {
        every { mock.callGeneric(any()) } returns "Hello!"
        assertEquals("Hello!", mock.callGeneric(""))
        verify { mock.callGeneric(any()) }
    }
}
