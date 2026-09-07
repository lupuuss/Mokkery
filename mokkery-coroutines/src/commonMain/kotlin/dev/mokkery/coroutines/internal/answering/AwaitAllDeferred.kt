package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.awaitAll

@Poko
internal class AwaitAllDeferred<T>(val deferreds: List<Deferred<T>>) : Awaitable<List<T>>, Renderable {
    override suspend fun await(scope: MokkerySuspendCallScope): List<T> = deferreds.awaitAll()

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "all(${deferreds.joinToString()})"
}
