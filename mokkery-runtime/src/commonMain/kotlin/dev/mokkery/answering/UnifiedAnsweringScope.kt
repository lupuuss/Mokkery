package dev.mokkery.answering

import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.internal.answering.AnsweringInterceptor
import dev.mokkery.internal.templating.CallTemplate

internal class UnifiedAnsweringScope<T>(
    private val answering: AnsweringInterceptor,
    private val template: CallTemplate,
) : SuspendAnsweringScope<T>, RegularAnsweringScope<T> {

    @DelicateMokkeryApi
    override fun answers(answer: Answer<T>) {
        answering.setup(template, answer)
    }
}
