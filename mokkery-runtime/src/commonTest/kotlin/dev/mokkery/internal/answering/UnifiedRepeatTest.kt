package dev.mokkery.internal.answering

import dev.mokkery.answering.Answer
import dev.mokkery.answering.Answer.Const
import dev.mokkery.test.callBlocking
import dev.mokkery.test.callSuspend
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class UnifiedRepeatTest {

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
        assertEquals(1, sequential.callBlocking())
        assertEquals(2, sequential.callBlocking())
        assertEquals(1, sequential.callBlocking())
        assertEquals(2, sequential.callBlocking())
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
        assertEquals(1, sequential.callSuspend())
        assertEquals(2, sequential.callSuspend())
        assertEquals(1, sequential.callSuspend())
        assertEquals(2, sequential.callSuspend())
    }

    @Test
    fun testThrowsEndOfRepeatBlockException() {
        assertFailsWith<EndOfRepeatBlockException> {
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
        assertEquals(1, sequential.callBlocking())
        assertEquals(2, sequential.callBlocking())
        assertEquals(2, sequential.callBlocking())
        assertEquals(2, sequential.callBlocking())
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
        assertEquals(1, sequential.callSuspend())
        assertEquals(2, sequential.callSuspend())
        assertEquals(2, sequential.callSuspend())
        assertEquals(2, sequential.callSuspend())
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
