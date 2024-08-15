package dev.mokkery.internal.answering

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.BlockingCallDefinitionScope
import dev.mokkery.answering.CallArgs
import dev.mokkery.test.fakeFunctionScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class CallsCatchingAnswerTest {

    private var block: BlockingCallDefinitionScope<Result<Int>>.(CallArgs) -> Int = { (i: Int) -> i }
    private val answer by lazy { CallsCatchingAnswer(block) }

    @Test
    fun testCallReturnsBlockReturnValueAsResult() {
        assertEquals(Result.success(2), answer.call(fakeFunctionScope(args = listOf(2))))
    }

    @Test
    fun testCallReturnsThrownExceptionAsResult() {
        val exception = IllegalStateException()
        block = { throw exception }
        assertEquals(Result.failure(exception), answer.call(fakeFunctionScope()))
    }

    @Test
    fun testCallRethrowsMokkeryRuntimeException() {
        val exception = MokkeryRuntimeException()
        block = { throw exception }
        assertEquals(exception, assertFails {  answer.call(fakeFunctionScope()) })
    }
}