package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.interceptor.MokkerySuspendCallScope
import kotlinx.coroutines.channels.ReceiveChannel

@Poko
internal class AwaitReceiveChannel<T>(
    private val channel: ReceiveChannel<T>
) : Awaitable<T> {

    override suspend fun await(scope: MokkerySuspendCallScope): T = channel.receive()

    override fun description(): String = "receive(from=Channel($channel))"
}
