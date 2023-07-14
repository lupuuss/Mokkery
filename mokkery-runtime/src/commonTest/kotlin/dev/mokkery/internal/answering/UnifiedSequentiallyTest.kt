package dev.mokkery.internal.answering

import dev.mokkery.answering.Answer
import dev.mokkery.internal.NoMoreSequentialAnswersException
import dev.mokkery.test.TestAnsweringScope
import dev.mokkery.test.fakeFunctionScope
import kotlinx.coroutines.test.runTest
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
        assertEquals(1, sequential.call(fakeFunctionScope()))
        assertEquals(2, sequential.call(fakeFunctionScope()))
        assertEquals(3, sequential.call(fakeFunctionScope()))
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
        assertEquals(1, sequential.callSuspend(fakeFunctionScope()))
        assertEquals(2, sequential.callSuspend(fakeFunctionScope()))
        assertEquals(3, sequential.callSuspend(fakeFunctionScope()))
    }

    @Test
    fun testRegisterSequentialAnswerThatOnCallThrowsWhenNoMoreAnswers() {
        scope.unifiedSequentially {
            answers(Answer.Const(1))
        }
        val sequential = scope.answers.single()
        assertIs<Answer.Sequential<Int>>(sequential)
        sequential.call(fakeFunctionScope())
        assertFailsWith<NoMoreSequentialAnswersException> {
            sequential.call(fakeFunctionScope())
        }
    }

    @Test
    fun testRegisterSequentialAnswerThatOnCallSuspendThrowsWhenNoMoreAnswers() = runTest {
        scope.unifiedSequentially {
            answers(Answer.Const(1))
        }
        val sequential = scope.answers.single()
        assertIs<Answer.Sequential<Int>>(sequential)
        sequential.callSuspend(fakeFunctionScope())
        assertFailsWith<NoMoreSequentialAnswersException> {
            sequential.callSuspend(fakeFunctionScope())
        }
    }

    @Test
    fun testRegistersAnswersEvenIfEndOfKeepBlockExceptionOccurred() {
        scope.unifiedSequentially {
            answers(Answer.Const(1))
            throw EndOfKeepBlockException()
        }
        val sequential = scope.answers.single()
        assertIs<Answer.Sequential<Int>>(sequential)
        assertEquals(1, sequential.call(fakeFunctionScope()))
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
