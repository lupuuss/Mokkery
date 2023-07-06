package dev.mokkery.answering

public infix fun <T> AnsweringScope<T>.returns(value: T) {
    answers(Answer.Const(value))
}

public infix fun <T> AnsweringScope<T>.throws(error: Throwable) {
    answers(Answer.Throws(error))
}

public infix fun <T> BlockingAnsweringScope<T>.calls(block: (CallArgs) -> T) {
    answers(Answer.Block(block))
}

public infix fun <T> SuspendAnsweringScope<T>.calls(block: suspend (CallArgs) -> T) {
    answers(Answer.BlockSuspend(block))
}
