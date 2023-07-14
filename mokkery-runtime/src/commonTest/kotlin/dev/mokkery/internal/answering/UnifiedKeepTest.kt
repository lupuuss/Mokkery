package dev.mokkery.internal.answering

import dev.mokkery.answering.Answer
import dev.mokkery.answering.Answer.Const
import dev.mokkery.test.fakeFunctionScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UnifiedKeepTest {

    private val builder = UnifiedSequentialAnswerBuilder<Int>()

    @Test
    fun testRegisterSequentialAnswerThatOnCallReturnsResultsInRepeat() {
        assertFails {
            builder.unifiedRepeat {
                answers(Const(1))
                answers(Const(2))
            }
        }
        val sequential = builder.answers.single()
        assertIs<Answer.Sequential<Int>>(sequential)
        assertEquals(1, sequential.call(fakeFunctionScope()))
        assertEquals(2, sequential.call(fakeFunctionScope()))
        assertEquals(1, sequential.call(fakeFunctionScope()))
        assertEquals(2, sequential.call(fakeFunctionScope()))
    }

    @Test
    fun testRegisterSequentialAnswerThatOnCallSuspendReturnsResultsInSequence() = runTest {
        assertFails {
            builder.unifiedRepeat {
                answers(Const(1))
                answers(Const(2))
            }
        }
        val sequential = builder.answers.single()
        assertIs<Answer.Sequential<Int>>(sequential)
        assertEquals(1, sequential.callSuspend(fakeFunctionScope()))
        assertEquals(2, sequential.callSuspend(fakeFunctionScope()))
        assertEquals(1, sequential.callSuspend(fakeFunctionScope()))
        assertEquals(2, sequential.callSuspend(fakeFunctionScope()))
    }

    @Test
    fun testThrowsEndOfKeepBlockException() {
        assertFailsWith<EndOfKeepBlockException> {
            builder.unifiedRepeat {  }
        }
    }

    @Test
    fun testRegisterSequentialAnswerThatOnCallReturnsResultsInSequenceWhenNestedRepeat() {
        assertFails {
            builder.unifiedRepeat {
                answers(Const(1))
                unifiedRepeat {
                    answers(Const(2))
                }
            }
        }
        val sequential = builder.answers.single()
        assertIs<Answer.Sequential<Int>>(sequential)
        assertEquals(1, sequential.call(fakeFunctionScope()))
        assertEquals(2, sequential.call(fakeFunctionScope()))
        assertEquals(2, sequential.call(fakeFunctionScope()))
        assertEquals(2, sequential.call(fakeFunctionScope()))
    }

    @Test
    fun testRegisterSequentialAnswerThatOnCallSuspendReturnsResultsInSequenceWhenNestedRepeat() = runTest {
        assertFails {
            builder.unifiedRepeat {
                answers(Const(1))
                unifiedRepeat {
                    answers(Const(2))
                }
            }
        }
        val sequential = builder.answers.single()
        assertIs<Answer.Sequential<Int>>(sequential)
        assertEquals(1, sequential.callSuspend(fakeFunctionScope()))
        assertEquals(2, sequential.callSuspend(fakeFunctionScope()))
        assertEquals(2, sequential.callSuspend(fakeFunctionScope()))
        assertEquals(2, sequential.callSuspend(fakeFunctionScope()))
    }

    @Test
    fun testPropagatesOtherExceptionsWithoutRegisteringAnswer() {
        assertFailsWith<IllegalArgumentException> {
            builder.unifiedRepeat {
                answers(Const(1))
                throw IllegalArgumentException()
            }
        }
        assertTrue(builder.answers.isEmpty())
    }
}
