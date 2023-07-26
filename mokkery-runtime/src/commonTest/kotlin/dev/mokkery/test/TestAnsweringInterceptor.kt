package dev.mokkery.test

import dev.mokkery.answering.Answer
import dev.mokkery.internal.answering.AnsweringInterceptor
import dev.mokkery.internal.templating.CallTemplate

internal class TestAnsweringInterceptor : AnsweringInterceptor, TestMokkeryInterceptor() {

    private val _answers = mutableMapOf<CallTemplate, Answer<*>>()
    override val answers: Map<CallTemplate, Answer<*>> = _answers

    var resetCalls = 0
    private set

    override fun setup(template: CallTemplate, answer: Answer<*>) {
        _answers[template] = answer
    }

    override fun reset() {
        resetCalls++
    }
}
