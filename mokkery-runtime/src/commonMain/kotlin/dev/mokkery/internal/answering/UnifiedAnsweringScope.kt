package dev.mokkery.internal.answering

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.Answer
import dev.mokkery.answering.BlockingAnsweringScope
import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.internal.calls.CallTemplate

internal class UnifiedAnsweringScope<T>(
    private val answering: AnsweringRegistry,
    private val template: CallTemplate,
) : SuspendAnsweringScope<T>, BlockingAnsweringScope<T> {

    @DelicateMokkeryApi
    override fun answers(answer: Answer<T>) {
        answering.setup(template, answer)
    }
}
