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

public interface Awaitable<out T> {

    public suspend fun await(scope: FunctionScope): T

    public fun description(): String

    public companion object {

        public val cancellation: Awaitable<Nothing> = AwaitCancellation

        public fun <T> all(deferred: List<Deferred<T>>): Awaitable<List<T>> = AwaitAllDeferred(deferred)

        public fun <T> all(vararg deferred: Deferred<T>): Awaitable<List<T>> = AwaitAllDeferred(deferred.toList())

        public fun <T> receive(from: ReceiveChannel<T>): Awaitable<T> = AwaitReceiveChannel(from)

        public fun <T> send(to: SendChannel<T>, argAt: Int): Awaitable<Unit> {
            return AwaitSendChannel(
                toChannel = to,
                element = { it.args[argAt] as T },
                elementDescription = { "argAt=$argAt" }
            )
        }

        public fun <T> send(to: SendChannel<T>, value: T): Awaitable<Unit> {
            return AwaitSendChannel(
                toChannel = to,
                element = { value },
                elementDescription = { "value=$value" }
            )
        }

        public fun <T> send(
            to: SendChannel<T>,
            valueBy: suspend (SuspendCallDefinitionScope<T>) -> T
        ): Awaitable<Unit> {
            return AwaitSendChannel(
                toChannel = to,
                element = { valueBy(SuspendCallDefinitionScope(it)) },
                elementDescription = { "valueBy={...}" }
            )
        }

        public fun send(to: SendChannel<Unit>): Awaitable<Unit> = send(to = to, value = Unit)

        public fun <T> delayed(value: T, by: Duration = 1.seconds): Awaitable<T> {
            return AwaitDelayed(duration = by, valueDescription = value::toString, value = { value })
        }

        public fun delayed(by: Duration = 1.seconds): Awaitable<Unit> {
            return delayed(by = by, value = Unit)
        }

        public fun <T> delayed(
            by: Duration = 1.seconds,
            valueBy: suspend (SuspendCallDefinitionScope<T>) -> T
        ): Awaitable<T> {
            return AwaitDelayed(
                duration = by,
                valueDescription = { "{...}" },
                value = { valueBy(SuspendCallDefinitionScope(it)) }
            )
        }
    }
}

