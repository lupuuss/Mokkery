package dev.mokkery.test

import dev.mokkery.answering.Answer
import dev.mokkery.internal.interceptor.AnsweringInterceptor
import dev.mokkery.internal.calls.CallTemplate

internal class TestAnsweringInterceptor : AnsweringInterceptor, TestMokkeryCallInterceptor() {

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
