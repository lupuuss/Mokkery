package dev.mokkery.answering

import dev.mokkery.test.fakeFunctionScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockAnswerTest {

    private var capturedArg: FunctionScope? = null
    private var answerResult: String = "1"
    private val answer = Answer.Block {
        capturedArg = it
        answerResult
    }

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


    @Test
    fun testBlockReceivesFunctionScopeOnCall() {
        answer.call(fakeFunctionScope(Int::class, 1))
        assertEquals(fakeFunctionScope(Int::class, 1), capturedArg)
        answer.call(fakeFunctionScope(String::class, "1"))
        assertEquals(fakeFunctionScope(String::class, "1"), capturedArg)
    }

    @Test
    fun testBlockReceivesFunctionScopeOnCallSuspend() = runTest {
        answer.callSuspend(fakeFunctionScope(Int::class, 1))
        assertEquals(fakeFunctionScope(Int::class, 1), capturedArg)
        answer.callSuspend(fakeFunctionScope(String::class, "1"))
        assertEquals(fakeFunctionScope(String::class, "1"), capturedArg)
    }

}
