package dev.mokkery.answering

import dev.mokkery.test.callBlocking
import dev.mokkery.test.callSuspend
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ThrowsAnswerTest {

    private val error = Exception()
    private val answer = Answer.Throws(error)

    @Test
    fun testCallThrowsSpecifiedError() {
        assertEquals(error, assertFails { answer.callBlocking() })
    }

    @Test
    fun testCallSuspendThrowsSpecifiedError() = runTest {
        assertEquals(error, assertFails { answer.callSuspend() })
    }
}
