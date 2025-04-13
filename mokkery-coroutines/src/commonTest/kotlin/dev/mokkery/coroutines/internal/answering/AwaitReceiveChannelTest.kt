package dev.mokkery.coroutines.internal.answering

import dev.mokkery.coroutines.await
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AwaitReceiveChannelTest {

    private val channel = Channel<Int>()
    private val awaitable = AwaitReceiveChannel(channel)

    @Test
    fun testReceivesFromChannelOnEachCall() = runTest {
        backgroundScope.launch { channel.send(1); channel.send(2) }
        assertEquals(1, awaitable.await())
        assertEquals(2, awaitable.await())
    }

    @Test
    fun testCreatesProperDescription() {
        assertEquals("receive(from=Channel(capacity=0,data=[]))", awaitable.description())
    }
}
