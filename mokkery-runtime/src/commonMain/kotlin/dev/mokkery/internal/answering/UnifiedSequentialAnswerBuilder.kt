package dev.mokkery.internal.answering

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.Answer
import dev.mokkery.answering.AnsweringScope
import dev.mokkery.answering.BlockingSequentialAnswerBuilder
import dev.mokkery.answering.SequentialAnswerBuilder
import dev.mokkery.answering.SuspendSequentialAnswerBuilder

internal class UnifiedSequentialAnswerBuilder<T>
    : SuspendSequentialAnswerBuilder<T>, BlockingSequentialAnswerBuilder<T> {

    private val _answers = mutableListOf<Answer<T>>()
    val answers: List<Answer<T>> = _answers

    @DelicateMokkeryApi
    override fun answers(answer: Answer<T>) {
        _answers.add(answer)
    }
}

internal class EndOfKeepBlockException : Exception()

@Suppress("UNCHECKED_CAST")
internal fun <R, T: SequentialAnswerBuilder<R>> T.unifiedRepeat(block: T.() -> Unit): Nothing {
    val scope = UnifiedSequentialAnswerBuilder<R>()
    try {
        block(scope as T)
    } catch (_: EndOfKeepBlockException) {

    }
    answers(Answer.SequentialByIterator(scope.answers.iterator().cycle()))
    throw EndOfKeepBlockException()
}

@Suppress("UNCHECKED_CAST")
internal fun <R, T : SequentialAnswerBuilder<R>> AnsweringScope<R>.unifiedSequentially(block: T.() -> Unit) {
    val scope = UnifiedSequentialAnswerBuilder<R>()
    try {
        block(scope as T)
    } catch (_: EndOfKeepBlockException) {

    }
    answers(Answer.SequentialByIterator(scope.answers.iterator()))
}

private fun <T> Iterator<T>.cycle(): Iterator<T> {
    val list = asSequence().toList()
    var i = 0
    return generateSequence { list[i++ % list.size] }.iterator()
}
