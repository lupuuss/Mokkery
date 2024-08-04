package dev.mokkery.coroutines.answering

import dev.mokkery.answering.SuspendAnsweringScope
import dev.mokkery.answering.SuspendCallDefinitionScope
import dev.mokkery.coroutines.internal.answering.AwaitAnswer
import dev.mokkery.coroutines.internal.answering.AwaitDeferred
import kotlinx.coroutines.Deferred

/**
 * Function call awaits for specified [awaitable].
 */
public infix fun <T> SuspendAnsweringScope<T>.awaits(awaitable: Awaitable<T>) {
    answers(AwaitAnswer(awaitable))
}

/**
 * Function call awaits for [deferred].
 */
public infix fun <T> SuspendAnsweringScope<T>.awaits(deferred: Deferred<T>) {
    awaits(AwaitDeferred(description = deferred::toString, deferred = { deferred }))
}

/**
 * Function call awaits on each call for a [Deferred] provided by [provider].
 */
public infix fun <T> SuspendAnsweringScope<T>.awaits(provider: (SuspendCallDefinitionScope<T>) -> Deferred<T>) {
    awaits(AwaitDeferred(description = { "{...}" }, deferred = { SuspendCallDefinitionScope<T>(it).let(provider) }))
}
