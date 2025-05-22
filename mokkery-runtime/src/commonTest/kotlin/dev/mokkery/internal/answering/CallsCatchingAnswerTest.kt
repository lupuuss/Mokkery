package dev.mokkery.internal.answering

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.BlockingCallDefinitionScope
import dev.mokkery.answering.CallArgs
import dev.mokkery.context.CallArgument
import dev.mokkery.test.callBlocking
import dev.mokkery.test.fakeFunctionCall
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CallsCatchingAnswerTest {

    private var block: BlockingCallDefinitionScope<Result<Int>>.(CallArgs) -> Int = { (i: Int) -> i }
    private val answer by lazy { CallsCatchingAnswer(block) }

    private val functionCall = fakeFunctionCall(
        returnType = Int::class,
        args = listOf(CallArgument(2, "i", Int::class, false))
    )

    @Test
    fun testCallReturnsBlockReturnValueAsResult() {
        assertEquals(Result.success(2), answer.callBlocking(functionCall))
    }

    @Test
    fun testCallReturnsThrownExceptionAsResult() {
        val exception = IllegalStateException()
        block = { throw exception }
        assertEquals(Result.failure(exception), answer.callBlocking(functionCall))
    }

    @Test
    fun testCallRethrowsMokkeryRuntimeException() {
        val exception = MokkeryRuntimeException()
        block = { throw exception }
        assertEquals(exception, assertFails {  answer.callBlocking(functionCall) })
    }
}
