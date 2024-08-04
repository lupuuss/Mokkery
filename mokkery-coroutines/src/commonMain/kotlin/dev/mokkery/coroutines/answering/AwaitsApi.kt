package dev.mokkery.coroutines.answering

import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.coroutines.internal.answering.AwaitAnswer

/**
 * Function call awaits for specified [awaitable].
 */
public infix fun <T> SuspendAnsweringScope<T>.awaits(awaitable: Awaitable<T>) {
    answers(AwaitAnswer(awaitable))
}
