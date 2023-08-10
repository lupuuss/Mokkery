package dev.mokkery.answering

import dev.mokkery.test.fakeFunctionScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BlockSuspendAnswerTest {
    private var capturedScope: FunctionScope? = null
    private var capturedArgs: CallArgs? = null
    private var answerResult: String = "1"
    private val answer = Answer.BlockSuspend {
        capturedScope = this
        capturedArgs = it
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
        answer.callSuspend(fakeFunctionScope(Int::class, args = listOf(1, 2, 3)))
        assertEquals(fakeFunctionScope(Int::class, args = listOf(1, 2, 3)), capturedScope)
    }

    @Test
    fun testBlockReceivesCallArgsOnCallSuspend() = runTest {
        answer.callSuspend(fakeFunctionScope(Int::class, args = listOf(1, 2, 3)))
        assertEquals(CallArgs(listOf(1, 2, 3)), capturedArgs)
    }

}
