package dev.mokkery.test

import dev.mokkery.answering.Answer
import dev.mokkery.MokkeryCallScope
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.answering.AnsweringRegistry
import dev.mokkery.internal.templating.CallTemplate

internal class TestAnsweringRegistry : AnsweringRegistry {

    private val _answers = mutableListOf<Pair<CallTemplate, Answer<*>>>()
    override val answers: List<Pair<CallTemplate, Answer<*>>> = _answers

    var resetCalls = 0
        private set

    override fun setup(template: CallTemplate, answer: Answer<*>) {
        _answers.removeAll { it.first == template }
        _answers.add(Pair(template, answer))
    }

    override fun reset() {
        resetCalls++
    }

    override fun resolveAnswer(scope: MokkeryCallScope) = throw CallNotMockedException("Tests")
}
