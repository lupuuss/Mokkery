package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.MokkerySuspendCallScope
import kotlinx.coroutines.Deferred

@Poko
internal class AwaitDeferred<T>(
    private val description: () -> String = { "{...}" },
    private val deferred: (scope: MokkerySuspendCallScope) -> Deferred<T>,
) : Awaitable<T> {

    override suspend fun await(scope: MokkerySuspendCallScope): T = deferred(scope).await()

    override fun description(): String = description.invoke()
}
