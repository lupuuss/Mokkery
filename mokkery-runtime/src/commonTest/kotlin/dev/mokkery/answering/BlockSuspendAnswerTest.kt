package dev.mokkery.answering

import dev.mokkery.test.fakeFunctionScope
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockSuspendAnswerTest {
    private var answerResult: String = "1"
    private val answer = Answer.BlockSuspend {
        answerResult
    }

    @Test
    fun testCallSuspendReturnsBlockResult() = runTest {
        assertEquals("1", answer.callSuspend(fakeFunctionScope()))
        answerResult = "2"
        assertEquals("2", answer.callSuspend(fakeFunctionScope()))
    }

}
