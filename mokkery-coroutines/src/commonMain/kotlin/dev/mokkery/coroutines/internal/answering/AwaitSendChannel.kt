package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import kotlinx.coroutines.channels.SendChannel

@Poko
internal class AwaitSendChannel<T>(
    private val toChannel: SendChannel<T>,
    private val element: suspend (MokkerySuspendCallScope) -> T,
    private val elementDescription: () -> String,
) : Awaitable<Unit>, Renderable {

    override suspend fun await(scope: MokkerySuspendCallScope) {
        toChannel.send(element(scope))
    }

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "send(to=Channel($toChannel), ${elementDescription()})"
}
