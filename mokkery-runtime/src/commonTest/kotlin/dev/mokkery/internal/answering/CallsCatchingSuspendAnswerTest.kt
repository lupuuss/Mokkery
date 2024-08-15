package dev.mokkery.internal.answering

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.CallArgs
import dev.mokkery.answering.SuspendCallDefinitionScope
import dev.mokkery.test.fakeFunctionScope
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CallsCatchingSuspendAnswerTest {

    private var block: suspend SuspendCallDefinitionScope<Result<Int>>.(CallArgs) -> Int = { (i: Int) -> i }
    private val answer by lazy { CallsCatchingSuspendAnswer(block) }

    @Test
    fun testCallSuspendReturnsBlockReturnValueAsResult() = runTest {
        assertEquals(Result.success(2), answer.callSuspend(fakeFunctionScope(args = listOf(2))))
    }

    @Test
    fun testCallSuspendReturnsThrownExceptionAsResult() = runTest {
        val exception = IllegalStateException()
        block = { throw exception }
        assertEquals(Result.failure(exception), answer.callSuspend(fakeFunctionScope()))
    }

    @Test
    fun testCallSuspendRethrowsMokkeryRuntimeException() = runTest {
        val exception = MokkeryRuntimeException()
        block = { throw exception }
        assertEquals(exception, assertFails {  answer.callSuspend(fakeFunctionScope()) })
    }

    @Test
    fun testFailsOnCall() {
        assertFails { answer.call(fakeFunctionScope()) }
    }
}