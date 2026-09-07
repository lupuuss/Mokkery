package dev.mokkery.coroutines.internal.answering

import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import kotlinx.coroutines.awaitCancellation

internal data object AwaitCancellation : Awaitable<Nothing>, Renderable {

    override suspend fun await(scope: MokkerySuspendCallScope): Nothing = awaitCancellation()

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "cancellation"
}
