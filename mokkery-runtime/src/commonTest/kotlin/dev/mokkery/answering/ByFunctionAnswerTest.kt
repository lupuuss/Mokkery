package dev.mokkery.answering

import dev.mokkery.internal.answering.ByFunctionAnswer
import dev.mokkery.test.fakeFunctionScope
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
        assertEquals(0, answer.call(fakeFunctionScope()))
    }

    @Test
    fun testRepeatingCallRepeatedlyCallsFunction() {
        assertEquals(0, answer.call(fakeFunctionScope()))
        assertEquals(1, answer.call(fakeFunctionScope()))
        assertEquals(2, answer.call(fakeFunctionScope()))
    }

    @Test
    fun testCallSuspendReturnsFunctionResult() = runTest {
        assertEquals(0, answer.callSuspend(fakeFunctionScope()))
    }

    @Test
    fun testRepeatingCallSuspendRepeatedlyCallsFunction() = runTest {
        assertEquals(0, answer.callSuspend(fakeFunctionScope()))
        assertEquals(1, answer.callSuspend(fakeFunctionScope()))
        assertEquals(2, answer.callSuspend(fakeFunctionScope()))
    }
}