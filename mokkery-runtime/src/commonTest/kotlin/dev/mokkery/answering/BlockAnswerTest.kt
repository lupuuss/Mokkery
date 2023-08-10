package dev.mokkery.answering

import dev.mokkery.test.fakeFunctionScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockAnswerTest {

    private var capturedScope: FunctionScope? = null
    private var capturedArgs: CallArgs? = null
    private var answerResult: String = "1"
    private val answer = Answer.Block {
        capturedScope = this
        capturedArgs = it
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
        answer.call(fakeFunctionScope(Int::class, args = listOf(1, 2, 3)))
        assertEquals(fakeFunctionScope(Int::class, args = listOf(1, 2, 3)), capturedScope)
    }

    @Test
    fun testBlockReceivesCallArgsOnCall() {
        answer.call(fakeFunctionScope(Int::class, args = listOf(1, 2, 3)))
        assertEquals(CallArgs(listOf(1, 2, 3)), capturedArgs)
    }

    @Test
    fun testBlockReceivesFunctionScopeOnCallSuspend() = runTest {
        answer.callSuspend(fakeFunctionScope(Int::class, args = listOf(1, 2, 3)))
        assertEquals(fakeFunctionScope(Int::class, args = listOf(1, 2, 3)), capturedScope)
    }

    @Test
    fun testBlockReceivesCallArgsOnCallSuspend() = runTest {
        answer.callSuspend(fakeFunctionScope(Int::class, args = listOf(1, 2, 3)))
        assertEquals(CallArgs(listOf(1, 2, 3)), capturedArgs)
    }

}
