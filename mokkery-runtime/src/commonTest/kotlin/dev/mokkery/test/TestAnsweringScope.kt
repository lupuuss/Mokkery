package dev.mokkery.test

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.Answer
import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope

class TestAnsweringScope<T> : BlockingAnsweringScope<T>, SuspendAnsweringScope<T> {

    private val _answers = mutableListOf<Answer<T>>()
    val answers: List<Answer<T>> = _answers

    @DelicateMokkeryApi
    override fun answers(answer: Answer<T>) {
        _answers.add(answer)
    }
}
