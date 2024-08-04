package dev.mokkery.coroutines.answering

import dev.mokkery.answering.FunctionScope
import dev.mokkery.coroutines.internal.answering.AwaitCancellation

public interface Awaitable<out T> {

    public suspend fun await(scope: FunctionScope): T

    public fun description(): String

    public companion object {

        public val cancellation: Awaitable<Nothing> = AwaitCancellation
    }
}

