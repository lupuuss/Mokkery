package dev.mokkery.answering

import dev.mokkery.test.fakeFunctionScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ThrowsAnswerTest {

    private val error = Exception()
    private val answer = Answer.Throws(error)

    @Test
    fun testCallThrowsSpecifiedError() {
        assertEquals(error, assertFails { answer.call(fakeFunctionScope()) })
    }

    @Test
    fun testCallSuspendThrowsSpecifiedError() = runTest {
        assertEquals(error, assertFails { answer.callSuspend(fakeFunctionScope()) })
    }
}
