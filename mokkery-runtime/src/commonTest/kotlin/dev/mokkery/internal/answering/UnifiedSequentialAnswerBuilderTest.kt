package dev.mokkery.internal.answering

import dev.mokkery.answering.Answer
import kotlin.test.Test
import kotlin.test.assertEquals

class UnifiedSequentialAnswerBuilderTest {

    private val builder = UnifiedSequentialAnswerBuilder<Int>()

    @Test
    fun testReturnsRegisteredAnswers() {
        val answersList = listOf(Answer.Const(1), Answer.Const(2))
        builder.answers(answersList[0])
        builder.answers(answersList[1])
        assertEquals<List<Answer<Int>>>(answersList, builder.answers)
    }
}
