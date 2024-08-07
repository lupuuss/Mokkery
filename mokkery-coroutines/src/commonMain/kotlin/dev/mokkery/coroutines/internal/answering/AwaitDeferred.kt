package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.answering.FunctionScope
import dev.mokkery.coroutines.answering.Awaitable
import kotlinx.coroutines.Deferred

@Poko
internal class AwaitDeferred<T>(
    private val description: () -> String = { "{...}" },
    private val deferred: (scope: FunctionScope) -> Deferred<T>,
) : Awaitable<T> {

    override suspend fun await(scope: FunctionScope): T = deferred(scope).await()

    override fun description(): String = description.invoke()
}
