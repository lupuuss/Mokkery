package dev.mokkery.answering

import dev.mokkery.internal.SuspendingAnswerBlockingCallException
import dev.mokkery.test.callBlocking
import dev.mokkery.test.callSuspend
import dev.mokkery.test.fakeFunctionCall
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class BlockSuspendAnswerTest {

    private var answerResult: String = "1"
    private val answer = Answer.BlockSuspend {
        answerResult
    }

    @Test
    fun testCallSuspendReturnsBlockResult() = runTest {
        assertEquals("1", answer.callSuspend(fakeFunctionCall()))
        answerResult = "2"
        assertEquals("2", answer.callSuspend(fakeFunctionCall()))
    }

    @Test
    fun testBlockingCallFails() {
        assertFailsWith<SuspendingAnswerBlockingCallException> {
            answer.callBlocking()
        }
    }

}
