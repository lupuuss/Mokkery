package dev.mokkery.answer

import dev.mokkery.internal.Mokkery
import dev.mokkery.internal.answer.ConstAnswer
import dev.mokkery.internal.answer.CustomAnswer
import dev.mokkery.internal.answer.CustomSuspendAnswer
import dev.mokkery.internal.answer.MockAnswer
import dev.mokkery.internal.answer.ThrowsAnswer
import dev.mokkery.internal.tracing.CallTemplate

public sealed class BaseMockAnswerScope<T>(
    private val mokkery: Mokkery,
    private val template: CallTemplate,
) {

    public infix fun returns(value: T): Unit = answersWith(ConstAnswer(value))

    public infix fun throws(throwable: Throwable): Unit = answersWith(ThrowsAnswer(throwable))

    internal fun answersWith(answer: MockAnswer<T>) {
        mokkery.mockCall(template, answer)
    }
}

public class MockAnswerScope<T> internal constructor(
    mokkery: Mokkery,
    template: CallTemplate
): BaseMockAnswerScope<T>(mokkery, template) {

    public infix fun calls(block: (CallArgs) -> T): Unit = answersWith(CustomAnswer(block))
}

public class MockSuspendAnswerScope<T> internal constructor(
    mokkery: Mokkery,
    template: CallTemplate
): BaseMockAnswerScope<T>(mokkery, template) {

    public infix fun calls(block: suspend (CallArgs) -> T): Unit = answersWith(CustomSuspendAnswer(block))
}
