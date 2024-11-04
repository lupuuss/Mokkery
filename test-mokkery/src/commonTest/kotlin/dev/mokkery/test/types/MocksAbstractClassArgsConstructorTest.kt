package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.AbstractClassArgsConstructor
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksAbstractClassArgsConstructorTest {

    private val mock = mock<AbstractClassArgsConstructor>()

    @Test
    fun testCall() {
        every { mock.call(any()) } returns 10
        assertEquals(10, mock.call(1))
        verify { mock.call(any()) }
    }
}
