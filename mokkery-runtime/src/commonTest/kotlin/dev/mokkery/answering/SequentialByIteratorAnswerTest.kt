package dev.mokkery.answering

import dev.mokkery.internal.NoMoreSequentialAnswersException
import dev.mokkery.test.fakeFunctionScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SequentialByIteratorAnswerTest {
    private var answers = listOf<Answer<Int>>()
    private val answer by lazy { Answer.SequentialByIterator(answers.iterator()) }

    @Test
    fun testAnswersFromIteratorInSequenceOnCall() {
        answers = listOf(1, 2, 3).map { Answer.Const(it) }
        assertEquals(1, answer.call(fakeFunctionScope()))
        assertEquals(2, answer.call(fakeFunctionScope()))
        assertEquals(3, answer.call(fakeFunctionScope()))
    }

    @Test
    fun testAnswersFromIteratorInSequenceOnCallSuspend() = runTest {
        answers = listOf(1, 2, 3).toAnswers()
        assertEquals(1, answer.callSuspend(fakeFunctionScope()))
        assertEquals(2, answer.callSuspend(fakeFunctionScope()))
        assertEquals(3, answer.callSuspend(fakeFunctionScope()))
    }

    @Test
    fun testThrowsNoMoreAnswerWhenEndOfSequenceOnCall() {
        answers = listOf(Answer.Const(1))
        assertEquals(1, answer.call(fakeFunctionScope()))
        assertFailsWith<NoMoreSequentialAnswersException> {
            answer.call(fakeFunctionScope())
        }
    }

    @Test
    fun testThrowsNoMoreAnswerWhenEndOfSequenceOnCallSuspend() = runTest {
        answers = listOf(Answer.Const(1))
        assertEquals(1, answer.callSuspend(fakeFunctionScope()))
        assertFailsWith<NoMoreSequentialAnswersException> {
            answer.callSuspend(fakeFunctionScope())
        }
    }

    @Test
    fun testCallsNestedSequentialUntilEmptyOnCall() {
        val nested = listOf(1, 2).toAnswers()
        answers = listOf(Answer.Const(0), Answer.SequentialByIterator(nested.iterator()), Answer.Const(3))
        assertEquals(0, answer.call(fakeFunctionScope()))
        assertEquals(1, answer.call(fakeFunctionScope()))
        assertEquals(2, answer.call(fakeFunctionScope()))
        assertEquals(3, answer.call(fakeFunctionScope()))
    }

    @Test
    fun testCallsNestedSequentialUntilEmptyOnSuspendCall() = runTest {
        val nested = listOf(1, 2).toAnswers()
        answers = listOf(Answer.Const(0), Answer.SequentialByIterator(nested.iterator()), Answer.Const(3))
        assertEquals(0, answer.callSuspend(fakeFunctionScope()))
        assertEquals(1, answer.callSuspend(fakeFunctionScope()))
        assertEquals(2, answer.callSuspend(fakeFunctionScope()))
        assertEquals(3, answer.callSuspend(fakeFunctionScope()))
    }

    private fun <T> List<T>.toAnswers() = map { Answer.Const(it) }
}
