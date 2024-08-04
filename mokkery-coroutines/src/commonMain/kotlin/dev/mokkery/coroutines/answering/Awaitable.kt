package dev.mokkery.coroutines.answering

import dev.mokkery.answering.FunctionScope

public interface Awaitable<out T> {

    public suspend fun await(scope: FunctionScope): T

    public fun description(): String

    public companion object {

    }
}

