package dev.mokkery.answering

import dev.mokkery.test.callBlocking
import dev.mokkery.test.callSuspend
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ConstAnswerTest {

    private val answer = Answer.Const(3)

    @Test
    fun testCallReturnsConstant() {
        assertEquals(3, answer.callBlocking())
    }

    @Test
    fun testCallSuspendReturnsConstant() = runTest {
        assertEquals(3, answer.callSuspend())
    }
}
