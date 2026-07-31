package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.AllDefaultsConstructor
import dev.mokkery.test.NestedDefaultsConstructor
import dev.mokkery.test.PartialDefaultsConstructor
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksDefaultArgsConstructorTest {

    @Test
    fun testCallWithAllDefaultConstructorArgs() {
        val mock = mock<AllDefaultsConstructor>()
        every { mock.call(any()) } returns 10
        assertEquals(10, mock.call(1))
        verify { mock.call(any()) }
    }

    @Test
    fun testCallWithSomeDefaultConstructorArgs() {
        val mock = mock<PartialDefaultsConstructor>()
        every { mock.call(any()) } returns 10
        assertEquals(10, mock.call(1))
        verify { mock.call(any()) }
    }

    @Test
    fun testCallWithStubbedParamThatHasDefaultConstructorArgs() {
        val mock = mock<NestedDefaultsConstructor>()
        every { mock.call(any()) } returns 10
        assertEquals(10, mock.call(1))
        verify { mock.call(any()) }
    }

    @Test
    fun testDefaultConstructorArgsAreActuallyApplied() {
        val mock = mock<AllDefaultsConstructor>()
        assertEquals(1, mock.param.value)
    }
}
