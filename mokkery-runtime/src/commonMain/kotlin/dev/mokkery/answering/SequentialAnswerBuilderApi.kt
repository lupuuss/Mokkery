package dev.mokkery.answering

import dev.mokkery.internal.answering.unifiedRepeat
import dev.mokkery.internal.answering.unifiedSequentially

/**
 * Function call will answer sequentially with answers defined in [block].
 *
 * Example:
 * ```kotlin
 * every { dependency.getString(any()) } sequentially {
 *    returns("1")
 *    returns("2")
 * }
 *
 * dependency.getString("a") // returns "1"
 * dependency.getString("a") // returns "2"
 * dependency.getString("a") // fails with no more answers
 * ```
 */
public infix fun <T> BlockingAnsweringScope<T>.sequentially(block: BlockingSequentialAnswerBuilder<T>.() -> Unit) {
    unifiedSequentially(block)
}

/**
 * Function call will answer sequentially with answers defined in [block].
 *
 * Example:
 * ```kotlin
 * everySuspend { dependency.getString(any()) } sequentially {
 *    returns("1")
 *    returns("2")
 * }
 *
 * dependency.getString("a") // returns "1"
 * dependency.getString("a") // returns "2"
 * dependency.getString("a") // fails with no more answers
 * ```
 */
public infix fun <T> SuspendAnsweringScope<T>.sequentially(block: SuspendSequentialAnswerBuilder<T>.() -> Unit) {
    unifiedSequentially(block)
}

/**
 * Allows to define a set of answers in [sequentially] that will repeat in cycles.
 *
 * It might be defined only once.
 *
 * Example:
 * ```kotlin
 * everySuspend { dependency.getString(any()) } sequentially {
 *    returns("1")
 *    repeat {
 *       returns("2")
 *    }
 * }
 *
 * dependency.getString("a") // returns "1"
 * dependency.getString("a") // returns "2"
 * dependency.getString("a") // returns "2"
 * ```
 */
public fun <T> SuspendSequentialAnswerBuilder<T>.repeat(block: SuspendSequentialAnswerBuilder<T>.() -> Unit): Nothing {
    unifiedRepeat(block)
}

/**
 * Allows to define a set of answers in [sequentially] that will repeat in cycles.
 *
 * It might be defined only once.
 *
 * Example:
 * ```kotlin
 * everySuspend { dependency.getString(any()) } sequentially {
 *    returns("1")
 *    repeat {
 *       returns("2")
 *    }
 * }
 *
 * dependency.getString("a") // returns "1"
 * dependency.getString("a") // returns "2"
 * dependency.getString("a") // returns "2"
 * ```
 */
public fun <T> BlockingSequentialAnswerBuilder<T>.repeat(block: BlockingSequentialAnswerBuilder<T>.() -> Unit): Nothing {
    unifiedRepeat(block)
}

/**
 * Simplification for [sequentially] with multiple [returns] calls.
 *
 * Just like [sequentially] throws exception when out of [values].
 */
public infix fun <T> AnsweringScope<T>.sequentiallyReturns(values: Iterable<T>) {
    unifiedSequentially {
        values.forEach { returns(it) }
    }
}

/**
 * Simplification for [sequentially] with multiple [throws] calls.
 *
 * Just like [sequentially] throws exception when out of [errors].
 */
public infix fun <T> AnsweringScope<T>.sequentiallyThrows(errors: Iterable<Throwable>) {
    unifiedSequentially {
        errors.forEach { throws(it) }
    }
}

/**
 * Simplification for [BlockingAnsweringScope.sequentially] with [repeat] [block].
 */
public infix fun <T> BlockingAnsweringScope<T>.sequentiallyRepeat(
    block: BlockingSequentialAnswerBuilder<T>.() -> Unit
) {
    sequentially { repeat { block() } }
}

/**
 * Simplification for [SuspendSequentialAnswerBuilder.sequentially] with [repeat] [block].
 */
public infix fun <T> SuspendSequentialAnswerBuilder<T>.sequentiallyRepeat(
    block: SuspendSequentialAnswerBuilder<T>.() -> Unit
) {
    sequentially { repeat { block() } }
}
