package dev.mokkery.coroutines.answering

import dev.mokkery.answering.FunctionScope
import dev.mokkery.answering.SuspendCallDefinitionScope
import dev.mokkery.coroutines.internal.answering.AwaitAllDeferred
import dev.mokkery.coroutines.internal.answering.AwaitCancellation
import dev.mokkery.coroutines.internal.answering.AwaitDelayed
import dev.mokkery.coroutines.internal.answering.AwaitReceiveChannel
import dev.mokkery.coroutines.internal.answering.AwaitSendChannel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Represents an operation that can be awaited during a mocked function call.
 * Result of this operation is returned by the mocked function call.
 */
public interface Awaitable<out T> {

    /**
     * Suspends current function call until the awaited result is available.
     */
    public suspend fun await(scope: FunctionScope): T

    /**
     * Provides a description of the awaitable action.
     */
    public fun description(): String

    public companion object {

        /**
         * Suspends indefinitely until cancelled.
         */
        public val cancellation: Awaitable<Nothing> = AwaitCancellation

        /**
         * Suspends until all [deferred] are completed and returns results as a [List].
         */
        public fun <T> all(deferred: List<Deferred<T>>): Awaitable<List<T>> = AwaitAllDeferred(deferred)

        /**
         * Suspends until all [deferred] are completed and returns results as a [List].
         */
        public fun <T> all(vararg deferred: Deferred<T>): Awaitable<List<T>> = AwaitAllDeferred(deferred.toList())

        /**
         * Suspends until an element is received from the channel.
         */
        public fun <T> receive(from: ReceiveChannel<T>): Awaitable<T> = AwaitReceiveChannel(from)

        /**
         * Suspends until an argument at specified index is send to the channel.
         */
        @Suppress("UNCHECKED_CAST")
        public fun <T> send(to: SendChannel<T>, argAt: Int): Awaitable<Unit> {
            return AwaitSendChannel(
                toChannel = to,
                element = { it.args[argAt] as T },
                elementDescription = { "argAt=$argAt" }
            )
        }

        /**
         * Suspends until the [value] is sent to the channel.
         */
        public fun <T> send(to: SendChannel<T>, value: T): Awaitable<Unit> {
            return AwaitSendChannel(
                toChannel = to,
                element = { value },
                elementDescription = { "value=$value" }
            )
        }

        /**
         * Suspends until a value provided on each call by the [valueProvider] is sent to the channel.
         */
        public fun <T> send(
            to: SendChannel<T>,
            valueProvider: suspend (SuspendCallDefinitionScope<T>) -> T
        ): Awaitable<Unit> {
            return AwaitSendChannel(
                toChannel = to,
                element = { valueProvider(SuspendCallDefinitionScope(it)) },
                elementDescription = { "valueProvider={...}" }
            )
        }

        /**
         * Suspends until the [Unit] is sent to the channel.
         */
        public fun send(to: SendChannel<Unit>): Awaitable<Unit> = send(to = to, value = Unit)

        /**
         * Delays for the specified duration and returns [value].
         */
        public fun <T> delayed(value: T, by: Duration = 1.seconds): Awaitable<T> {
            return AwaitDelayed(duration = by, valueDescription = value::toString, value = { value })
        }

        /**
         * Delays for the specified duration and returns [Unit].
         */
        public fun delayed(by: Duration = 1.seconds): Awaitable<Unit> {
            return delayed(by = by, value = Unit)
        }

        /**
         * Delays for the specified duration and returns value provided on each call by [valueProvider].
         */
        public fun <T> delayed(
            by: Duration = 1.seconds,
            valueProvider: suspend (SuspendCallDefinitionScope<T>) -> T
        ): Awaitable<T> {
            return AwaitDelayed(
                duration = by,
                valueDescription = { "valueProvider={...}" },
                value = { valueProvider(SuspendCallDefinitionScope(it)) }
            )
        }
    }
}

