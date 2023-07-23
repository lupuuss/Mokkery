package dev.mokkery.test

import dev.mokkery.MockMode.autofill
import dev.mokkery.MokkeryRuntimeException
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

    private val mocked = mock<TestInterface>(autofill)
    private val spied = spy(mocked)

    @Test
    fun testResetCallsOnMock() {
        mocked.callUnit()
        resetCalls(mocked)
        assertFailsWith<AssertionError> {
            verify {
                spied.callUnit()
            }
        }
    }

    @Test
    fun testResetCallsOnSpy() {
        spied.callUnit()
        resetCalls(spied)
        assertFailsWith<AssertionError> {
            verify {
                spied.callUnit()
            }
        }
    }

    @Test
    fun testResetAnswersFailsOnSpy() {
        assertFailsWith<MokkeryRuntimeException> {
            resetAnswers(spied)
        }
    }

    @Test
    fun testResetAnswersOnMock() {
        every { mocked.callWithPrimitives(1) } returns 1.0
        resetAnswers(mocked)
        assertEquals(0.0, mocked.callWithPrimitives(1))
    }
}
