package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.test.AbstractGenericClassArgsConstructor
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksAbstractGenericClassArgsConstructorTest {

    private val mock = mock<AbstractGenericClassArgsConstructor<Int>>()

    @Test
    fun testValue() {
        every { mock.value } returns 1
        assertEquals(1, mock.value)
        verify { mock.value }
    }
}
