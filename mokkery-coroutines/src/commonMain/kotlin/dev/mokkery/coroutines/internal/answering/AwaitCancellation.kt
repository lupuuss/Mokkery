package dev.mokkery.coroutines.internal.answering

import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.rendering.MokkeryRenderingScope
import kotlinx.coroutines.awaitCancellation

internal data object AwaitCancellation : Awaitable<Nothing> {

    override suspend fun await(scope: MokkerySuspendCallScope): Nothing = awaitCancellation()

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "cancellation"
}
