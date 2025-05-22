package dev.mokkery.internal.answering

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.CallArgs
import dev.mokkery.answering.SuspendCallDefinitionScope
import dev.mokkery.context.CallArgument
import dev.mokkery.test.callBlocking
import dev.mokkery.test.callSuspend
import dev.mokkery.test.fakeFunctionCall
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CallsCatchingSuspendAnswerTest {

    private var block: suspend SuspendCallDefinitionScope<Result<Int>>.(CallArgs) -> Int = { (i: Int) -> i }
    private val answer by lazy { CallsCatchingSuspendAnswer(block) }

    private val functionCall = fakeFunctionCall(
        returnType = Int::class,
        args = listOf(CallArgument(2, "i", Int::class, false))
    )

    @Test
    fun testCallSuspendReturnsBlockReturnValueAsResult() = runTest {
        assertEquals(Result.success(2), answer.callSuspend(functionCall))
    }

    @Test
    fun testCallSuspendReturnsThrownExceptionAsResult() = runTest {
        val exception = IllegalStateException()
        block = { throw exception }
        assertEquals(Result.failure(exception), answer.callSuspend(functionCall))
    }

    @Test
    fun testCallSuspendRethrowsMokkeryRuntimeException() = runTest {
        val exception = MokkeryRuntimeException()
        block = { throw exception }
        assertEquals(exception, assertFails {  answer.callSuspend(functionCall) })
    }

    @Test
    fun testFailsOnCall() {
        assertFails { answer.callBlocking(functionCall) }
    }
}
