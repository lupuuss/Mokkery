package dev.mokkery.test

import dev.mokkery.answering.Answer
import dev.mokkery.interceptor.MokkeryCallScope
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.answering.AnsweringRegistry
import dev.mokkery.internal.calls.CallTemplate

internal class TestAnsweringRegistry : AnsweringRegistry {

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

    override fun resolveAnswer(scope: MokkeryCallScope) = throw CallNotMockedException("Tests")
}
