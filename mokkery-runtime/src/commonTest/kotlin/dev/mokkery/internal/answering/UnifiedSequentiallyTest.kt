package dev.mokkery.internal.answering

import dev.mokkery.answering.Answer
import dev.mokkery.internal.NoMoreSequentialAnswersException
import dev.mokkery.test.TestAnsweringScope
import dev.mokkery.test.callBlocking
import dev.mokkery.test.callSuspend
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UnifiedSequentiallyTest {

    private val scope = TestAnsweringScope<Int>()

    @Test
    fun testRegisterSequentialAnswerThatOnCallReturnsResults() {
        scope.unifiedSequentially {
            answers(Answer.Const(1))
            answers(Answer.Const(2))
            answers(Answer.Const(3))
        }
        val sequential = scope.answers.single()
        assertIs<Answer.Sequential<Int>>(sequential)
        assertEquals(1, sequential.callBlocking())
        assertEquals(2, sequential.callBlocking())
        assertEquals(3, sequential.callBlocking())
    }

    @Test
    fun testRegisterSequentialAnswerThatOnCallSuspendReturnsResults() = runTest {
        scope.unifiedSequentially {
            answers(Answer.Const(1))
            answers(Answer.Const(2))
            answers(Answer.Const(3))
        }
        val sequential = scope.answers.single()
        assertIs<Answer.Sequential<Int>>(sequential)
        assertEquals(1, sequential.callSuspend())
        assertEquals(2, sequential.callSuspend())
        assertEquals(3, sequential.callSuspend())
    }

    @Test
    fun testRegisterSequentialAnswerThatOnCallThrowsWhenNoMoreAnswers() {
        scope.unifiedSequentially {
            answers(Answer.Const(1))
        }
        val sequential = scope.answers.single()
        assertIs<Answer.Sequential<Int>>(sequential)
        sequential.callBlocking()
        assertFailsWith<NoMoreSequentialAnswersException> {
            sequential.callBlocking()
        }
    }

    @Test
    fun testRegisterSequentialAnswerThatOnCallSuspendThrowsWhenNoMoreAnswers() = runTest {
        scope.unifiedSequentially {
            answers(Answer.Const(1))
        }
        val sequential = scope.answers.single()
        assertIs<Answer.Sequential<Int>>(sequential)
        sequential.callSuspend()
        assertFailsWith<NoMoreSequentialAnswersException> {
            sequential.callSuspend()
        }
    }

    @Test
    fun testRegistersAnswersEvenIfEndOfRepeatBlockExceptionOccurred() {
        scope.unifiedSequentially {
            answers(Answer.Const(1))
            throw EndOfRepeatBlockException()
        }
        val sequential = scope.answers.single()
        assertIs<Answer.Sequential<Int>>(sequential)
        assertEquals(1, sequential.callBlocking())
    }

    @Test
    fun testPropagatesOtherExceptionsWithoutRegisteringAnswer() {
        assertFailsWith<IllegalArgumentException> {
            scope.unifiedSequentially {
                answers(Answer.Const(1))
                throw IllegalArgumentException()
            }
        }
        assertTrue(scope.answers.isEmpty())
    }
}
