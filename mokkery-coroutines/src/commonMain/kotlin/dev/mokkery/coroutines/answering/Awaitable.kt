package dev.mokkery.coroutines.answering

import dev.mokkery.answering.FunctionScope
import dev.mokkery.coroutines.internal.answering.AwaitAllDeferred
import dev.mokkery.coroutines.internal.answering.AwaitCancellation
import kotlinx.coroutines.Deferred

public interface Awaitable<out T> {

    public suspend fun await(scope: FunctionScope): T

    public fun description(): String

    public companion object {

        public val cancellation: Awaitable<Nothing> = AwaitCancellation

        public fun <T> all(deferred: List<Deferred<T>>): Awaitable<List<T>> = AwaitAllDeferred(deferred)

        public fun <T> all(vararg deferred: Deferred<T>): Awaitable<List<T>> = AwaitAllDeferred(deferred.toList())
    }
}

