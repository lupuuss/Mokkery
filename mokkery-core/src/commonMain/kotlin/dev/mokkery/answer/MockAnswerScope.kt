package dev.mokkery.answer

import dev.mokkery.internal.answer.AnsweringInterceptor
import dev.mokkery.internal.answer.ConstAnswer
import dev.mokkery.internal.answer.CustomAnswer
import dev.mokkery.internal.answer.CustomSuspendAnswer
import dev.mokkery.internal.answer.MockAnswer
import dev.mokkery.internal.answer.ThrowsAnswer
import dev.mokkery.internal.templating.CallTemplate

public sealed class BaseMockAnswerScope<T>(
    private val answering: AnsweringInterceptor,
    private val template: CallTemplate,
) {

    public infix fun returns(value: T): Unit = answersWith(ConstAnswer(value))

    public infix fun throws(throwable: Throwable): Unit = answersWith(ThrowsAnswer(throwable))

    internal fun answersWith(answer: MockAnswer<T>) {
        answering.setup(template, answer)
    }
}

public class MockAnswerScope<T> internal constructor(
    answering: AnsweringInterceptor,
    template: CallTemplate
): BaseMockAnswerScope<T>(answering, template) {

    public infix fun calls(block: (CallArgs) -> T): Unit = answersWith(CustomAnswer(block))
}

public class MockSuspendAnswerScope<T> internal constructor(
    answering: AnsweringInterceptor,
    template: CallTemplate
): BaseMockAnswerScope<T>(answering, template) {

    public infix fun calls(block: suspend (CallArgs) -> T): Unit = answersWith(CustomSuspendAnswer(block))
}
