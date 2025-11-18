package dev.mokkery.test

import dev.mokkery.MockMode.autofill
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import dev.mokkery.resetAnswers
import dev.mokkery.resetCalls
import dev.mokkery.spy
import dev.mokkery.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResetTest {

    private val mocked = mock<RegularMethodsInterface>(autofill)
    private val spied = spy(mocked)

    @Test
    fun testResetCallsOnMock() {
        mocked.callUnit(Unit)
        resetCalls(mocked)
        assertFailsWith<AssertionError> {
            verify {
                spied.callUnit(Unit)
            }
        }
    }

    @Test
    fun testResetCallsOnSpy() {
        spied.callUnit(Unit)
        resetCalls(spied)
        assertFailsWith<AssertionError> {
            verify {
                spied.callUnit(Unit)
            }
        }
    }

    @Test
    fun testResetAnswersOnMock() {
        every { mocked.callPrimitive(1) } returns 1
        resetAnswers(mocked)
        assertEquals(0, mocked.callPrimitive(1))
    }

    @Test
    fun testResetAnswersOnSpy() {
        every { spied.callPrimitive(1) } returns 1
        resetAnswers(spied)
        assertEquals(0, mocked.callPrimitive(1))
    }
}
