package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.rendering.MokkeryRenderingScope
import dev.mokkery.rendering.Renderable
import kotlinx.coroutines.channels.ReceiveChannel

@Poko
internal class AwaitReceiveChannel<T>(
    private val channel: ReceiveChannel<T>
) : Awaitable<T>, Renderable {

    override suspend fun await(scope: MokkerySuspendCallScope): T = channel.receive()

    context(scope: MokkeryRenderingScope)
    override fun render(): String = "receive(from=Channel($channel))"
}
