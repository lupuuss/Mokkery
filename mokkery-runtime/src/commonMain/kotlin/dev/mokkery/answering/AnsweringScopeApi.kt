package dev.mokkery.answering

import dev.mokkery.internal.answering.ReturnsArgAtAnswer

/**
 * Function call always returns [value].
 */
public infix fun <T> AnsweringScope<T>.returns(value: T) {
    answers(Answer.Const(value))
}

/**
 * Function call returns [Result.success] with [value].
 */
public infix fun <T> AnsweringScope<in Result<T>>.returnsSuccess(value: T) {
    returns(Result.success(value))
}

/**
 * Function call returns [Result.failure] with [error].
 */
public infix fun <T> AnsweringScope<in Result<T>>.returnsFailure(error: Throwable) {
    returns(Result.failure(error))
}

/**
 * Function call returns argument at [index].
 */
public infix fun <T> AnsweringScope<T>.returnsArgAt(index: Int) {
    answers(ReturnsArgAtAnswer(index))
}

/**
 * Function call always throws [error].
 */
public infix fun <T> AnsweringScope<T>.throws(error: Throwable) {
    answers(Answer.Throws(error))
}

/**
 * Function call always throws an [IllegalStateException] with given [message].
 *
 * This is equivalent of [kotlin.error].
 */
public infix fun <T> AnsweringScope<T>.throwsErrorWith(message: Any) {
    answers(Answer.Throws(IllegalStateException(message.toString())))
}

/**
 * Function call executes [block].
 */
public infix fun <T> BlockingAnsweringScope<T>.calls(block: BlockingCallDefinitionScope<T>.(CallArgs) -> T) {
    answers(Answer.Block(block))
}

/**
 * Suspend function call executes [block].
 */
public infix fun <T> SuspendAnsweringScope<T>.calls(block: suspend SuspendCallDefinitionScope<T>.(CallArgs) -> T) {
    answers(Answer.BlockSuspend(block))
}

