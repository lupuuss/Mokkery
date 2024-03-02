package dev.mokkery.answering

import dev.mokkery.test.fakeFunctionScope
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockAnswerTest {


    private var answerResult: String = "1"
    private val answer = Answer.Block { answerResult }

    @Test
    fun testCallReturnsBlockResult() {
        assertEquals("1", answer.call(fakeFunctionScope()))
        answerResult = "2"
        assertEquals("2", answer.call(fakeFunctionScope()))
    }

    @Test
    fun testCallSuspendReturnsBlockResult() = runTest {
        assertEquals("1", answer.callSuspend(fakeFunctionScope()))
        answerResult = "2"
        assertEquals("2", answer.callSuspend(fakeFunctionScope()))
    }

}
