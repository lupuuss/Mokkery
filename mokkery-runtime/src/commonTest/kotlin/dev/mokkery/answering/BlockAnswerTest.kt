package dev.mokkery.answering

import dev.mokkery.internal.BlockingAnswerSuspendingCallException
import dev.mokkery.test.callBlocking
import dev.mokkery.test.callSuspend
import dev.mokkery.test.fakeFunctionCall
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BlockAnswerTest {


    private var answerResult: String = "1"
    private val answer = Answer.Block { answerResult }

    @Test
    fun testCallReturnsBlockResult() {
        assertEquals("1", answer.callBlocking(fakeFunctionCall()))
        answerResult = "2"
        assertEquals("2", answer.callBlocking(fakeFunctionCall()))
    }

    @Test
    fun testSuspendCallFails() = runTest {
        assertFailsWith<BlockingAnswerSuspendingCallException> { answer.callSuspend() }
    }

}
