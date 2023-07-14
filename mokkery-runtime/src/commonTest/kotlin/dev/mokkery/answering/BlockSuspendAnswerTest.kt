package dev.mokkery.answering

import dev.mokkery.test.fakeFunctionScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockSuspendAnswerTest {
    private var capturedArg: FunctionScope? = null
    private var answerResult: String = "1"
    private val answer = Answer.BlockSuspend {
        capturedArg = it
        answerResult
    }

    @Test
    fun testCallSuspendReturnsBlockResult() = runTest {
        assertEquals("1", answer.callSuspend(fakeFunctionScope()))
        answerResult = "2"
        assertEquals("2", answer.callSuspend(fakeFunctionScope()))
    }

    @Test
    fun testBlockReceivesFunctionScopeOnCallSuspend() = runTest {
        answer.callSuspend(fakeFunctionScope(Int::class, 1))
        assertEquals(fakeFunctionScope(Int::class, 1), capturedArg)
        answer.callSuspend(fakeFunctionScope(String::class, "1"))
        assertEquals(fakeFunctionScope(String::class, "1"), capturedArg)
    }

}
