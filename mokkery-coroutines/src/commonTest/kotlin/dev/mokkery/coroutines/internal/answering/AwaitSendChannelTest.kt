package dev.mokkery.coroutines.internal.answering

import dev.mokkery.coroutines.await
import dev.mokkery.coroutines.createMokkerySuspendCallScope
import dev.mokkery.interceptor.MokkerySuspendCallScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class AwaitSendChannelTest {

    private var elementDescription = "value=Unit"
    private val channel = Channel<Int>()
    private var element: suspend (MokkerySuspendCallScope) -> Int = { 1 }
    private val awaitable = AwaitSendChannel(
        toChannel = channel,
        elementDescription = { elementDescription },
        element = { element(it) }
    )

    @Test
    fun testComposesProperDescription() {
        assertEquals("send(to=Channel(capacity=0,data=[]), value=Unit)", awaitable.description())
    }

    @Test
    fun testSendsProvidedElementOnEachCall() = runTest {
        backgroundScope.launch {
            awaitable.await()
            element = { 2 }
            awaitable.await()
        }
        assertEquals(1, channel.receive())
        assertEquals(2, channel.receive())
    }

    @Test
    fun testPassesCorrectScopeToDeferredProvider() = runTest {
        backgroundScope.launch { channel.receive() }
        var passedScope: MokkerySuspendCallScope? = null
        element = { passedScope = it ; 1 }
        val scope = createMokkerySuspendCallScope()
        awaitable.await(scope)
        assertSame(scope, passedScope)
    }
}
