package dev.mokkery.answering

/**
 * Function call always returns [value].
 */
public infix fun <T> AnsweringScope<T>.returns(value: T) {
    answers(Answer.Const(value))
}

/**
 * Function call always throws [error].
 */
public infix fun <T> AnsweringScope<T>.throws(error: Throwable) {
    answers(Answer.Throws(error))
}

/**
 * Function call executes [block].
 */
public infix fun <T> BlockingAnsweringScope<T>.calls(block: (CallArgs) -> T) {
    answers(Answer.Block(block))
}

/**
 * Suspend function call executes [block].
 */
public infix fun <T> SuspendAnsweringScope<T>.calls(block: suspend (CallArgs) -> T) {
    answers(Answer.BlockSuspend(block))
}
