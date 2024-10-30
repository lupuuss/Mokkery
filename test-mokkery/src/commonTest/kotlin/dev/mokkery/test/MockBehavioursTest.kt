package dev.mokkery.test

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class MockBehavioursTest {

    private val mock = mock<RegularMethodsInterface>()

    @Test
    fun testMockAnswersAreResolvedInReversedOrder() {
        every { mock.callPrimitive(any()) } returns -1
        every { mock.callPrimitive(2) } returns 2
        every { mock.callPrimitive(4) } returns 4
        every { mock.callPrimitive(6) } returns 6
        assertEquals(-1, mock.callPrimitive(0))
        assertEquals(-1, mock.callPrimitive(1))
        assertEquals(2, mock.callPrimitive(2))
        assertEquals(-1, mock.callPrimitive(3))
        assertEquals(4, mock.callPrimitive(4))
        assertEquals(6, mock.callPrimitive(6))
        assertEquals(-1, mock.callPrimitive(7))
    }
}
