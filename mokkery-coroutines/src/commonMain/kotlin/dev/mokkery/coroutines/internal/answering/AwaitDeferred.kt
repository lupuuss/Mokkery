package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import kotlinx.coroutines.Deferred

@Poko
internal class AwaitDeferred<T>(
    private val description: () -> String = { "{...}" },
    private val deferred: (scope: MokkerySuspendCallScope) -> Deferred<T>,
) : Awaitable<T>, Renderable {

    override suspend fun await(scope: MokkerySuspendCallScope): T = deferred(scope).await()

    context(scope: MokkeryRenderingScope)
    override fun render(): String = description.invoke()
}
