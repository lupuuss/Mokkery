package dev.mokkery.test.types

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.test.FinalClass
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals

class MocksFinalClassTest {

    private val mock = mock<FinalClass>()

    @Test
    fun testFinalMethod() {
        every { mock.finalMethod() } returns "Hello!"
        assertEquals("Hello!", mock.finalMethod())
        verify { mock.finalMethod() }
    }
}
