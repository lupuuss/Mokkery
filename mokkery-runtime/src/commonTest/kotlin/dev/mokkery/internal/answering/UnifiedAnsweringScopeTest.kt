package dev.mokkery.internal.answering

import dev.mokkery.answering.Answer
import dev.mokkery.test.TestAnsweringRegistry
import dev.mokkery.test.fakeCallTemplate
import kotlin.test.Test
import kotlin.test.assertEquals

class UnifiedAnsweringScopeTest {

    private val template = fakeCallTemplate()
    private val answering = TestAnsweringRegistry()
    private val scope = UnifiedAnsweringScope<Int>(answering, template)

    @Test
    fun testAddsAnswerForGivenTemplate() {
        val answer = Answer.Const(1)
        scope.answers(answer)
        assertEquals(answer, answering.answers[template])
    }

    @Test
    fun testOverwritesAnswerForGivenTemplate() {
        scope.answers(Answer.Const(1))
        val answer = Answer.Const(0)
        scope.answers(answer)
        assertEquals(answer, answering.answers[template])
    }
}
