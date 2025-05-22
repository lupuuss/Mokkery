package dev.mokkery.answering

import dev.mokkery.internal.MokkeryBlockingCallScope
import dev.mokkery.internal.answering.ByFunctionAnswer
import dev.mokkery.test.callBlocking
import dev.mokkery.test.callSuspend
import dev.mokkery.test.fakeFunctionCall
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ByFunctionAnswerTest {

    private var value: Int = 0
    private val answer = ByFunctionAnswer(
        description = "answerDescription",
        block = { value++ }
    )

    @Test
    fun testDescriptionReturnsPassedArg() {
        assertEquals("answerDescription", answer.description())
    }

    @Test
    fun testCallReturnsFunctionResult() {
        assertEquals(0, answer.call(MokkeryBlockingCallScope(fakeFunctionCall())))
    }

    @Test
    fun testRepeatingCallRepeatedlyCallsFunction() {
        assertEquals(0, answer.callBlocking())
        assertEquals(1, answer.callBlocking())
        assertEquals(2, answer.callBlocking())
    }

    @Test
    fun testCallSuspendReturnsFunctionResult() = runTest {
        assertEquals(0, answer.callSuspend())
    }

    @Test
    fun testRepeatingCallSuspendRepeatedlyCallsFunction() = runTest {
        assertEquals(0, answer.callSuspend())
        assertEquals(1, answer.callSuspend())
        assertEquals(2, answer.callSuspend())
    }
}
