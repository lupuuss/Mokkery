package dev.mokkery.coroutines.internal.answering

import dev.drewhamilton.poko.Poko
import dev.mokkery.answering.FunctionScope
import dev.mokkery.coroutines.answering.Awaitable
import kotlinx.coroutines.channels.SendChannel

@Poko
internal class AwaitSendChannel<T>(
    private val toChannel: SendChannel<T>,
    private val element: suspend (FunctionScope) -> T,
    private val elementDescription: () -> String,
) : Awaitable<Unit> {

    override suspend fun await(scope: FunctionScope) {
        toChannel.send(element(scope))
    }

    override fun description(): String = "send(to=Channel($toChannel), ${elementDescription()})"
}
