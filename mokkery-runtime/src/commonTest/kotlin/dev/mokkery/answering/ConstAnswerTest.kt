package dev.mokkery.answering

import dev.mokkery.test.fakeFunctionScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstAnswerTest {

    private val answer = Answer.Const(3)

    @Test
    fun testCallReturnsConstant() {
        assertEquals(3, answer.call(fakeFunctionScope()))
    }

    @Test
    fun testCallSuspendReturnsConstant() = runTest {
        assertEquals(3, answer.callSuspend(fakeFunctionScope()))
    }
}
