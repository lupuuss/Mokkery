package dev.mokkery.answering

import dev.mokkery.internal.NoMoreSequentialAnswersException
import dev.mokkery.test.callBlocking
import dev.mokkery.test.callSuspend
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SequentialByIteratorAnswerTest {
    private var answers = listOf<Answer<Int>>()
    private val answer by lazy { Answer.SequentialByIterator(answers.iterator()) }

    @Test
    fun testAnswersFromIteratorInSequenceOnCall() {
        answers = listOf(1, 2, 3).map { Answer.Const(it) }
        assertEquals(1, answer.callBlocking())
        assertEquals(2, answer.callBlocking())
        assertEquals(3, answer.callBlocking())
    }

    @Test
    fun testAnswersFromIteratorInSequenceOnCallSuspend() = runTest {
        answers = listOf(1, 2, 3).toAnswers()
        assertEquals(1, answer.callSuspend())
        assertEquals(2, answer.callSuspend())
        assertEquals(3, answer.callSuspend())
    }

    @Test
    fun testThrowsNoMoreAnswerWhenEndOfSequenceOnCall() {
        answers = listOf(Answer.Const(1))
        assertEquals(1, answer.callBlocking())
        assertFailsWith<NoMoreSequentialAnswersException> {
            answer.callBlocking()
        }
    }

    @Test
    fun testThrowsNoMoreAnswerWhenEndOfSequenceOnCallSuspend() = runTest {
        answers = listOf(Answer.Const(1))
        assertEquals(1, answer.callSuspend())
        assertFailsWith<NoMoreSequentialAnswersException> {
            answer.callSuspend()
        }
    }

    @Test
    fun testCallsNestedSequentialUntilEmptyOnCall() {
        val nested = listOf(1, 2).toAnswers()
        answers = listOf(Answer.Const(0), Answer.SequentialByIterator(nested.iterator()), Answer.Const(3))
        assertEquals(0, answer.callBlocking())
        assertEquals(1, answer.callBlocking())
        assertEquals(2, answer.callBlocking())
        assertEquals(3, answer.callBlocking())
    }

    @Test
    fun testCallsNestedSequentialUntilEmptyOnSuspendCall() = runTest {
        val nested = listOf(1, 2).toAnswers()
        answers = listOf(Answer.Const(0), Answer.SequentialByIterator(nested.iterator()), Answer.Const(3))
        assertEquals(0, answer.callSuspend())
        assertEquals(1, answer.callSuspend())
        assertEquals(2, answer.callSuspend())
        assertEquals(3, answer.callSuspend())
    }


    @Test
    fun testAllowsMultiLevelNesting() {
        val nest4 = Answer.SequentialByIterator(
            listOf(Answer.Const(8), Answer.Const(9)).iterator()
        )
        val nest3 = Answer.SequentialByIterator(
            listOf(Answer.Const(6), Answer.Const(7), nest4).iterator()
        )
        val nest2 = Answer.SequentialByIterator(
            listOf(Answer.Const(4), Answer.Const(5), nest3).iterator()
        )
        val nest1 = Answer.SequentialByIterator(
            listOf(Answer.Const(2), Answer.Const(3), nest2).iterator()
        )
        answers = listOf(Answer.Const(0), Answer.Const(1), nest1)
        val result = generateSequence { answer.callBlocking() }
            .take(10)
            .toList()
        assertEquals(List(10) { it },  result)
    }

    private fun <T> List<T>.toAnswers() = map { Answer.Const(it) }
}
