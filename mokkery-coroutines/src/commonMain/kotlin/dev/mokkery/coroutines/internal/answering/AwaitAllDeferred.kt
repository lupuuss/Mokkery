package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.answering.FunctionScope
import dev.mokkery.coroutines.answering.Awaitable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll

@Poko
internal class AwaitAllDeferred<T>(val deferreds: List<Deferred<T>>) : Awaitable<List<T>> {
    override suspend fun await(scope: FunctionScope): List<T> = deferreds.awaitAll()

    override fun description(): String = "all(${deferreds.joinToString()})"
}