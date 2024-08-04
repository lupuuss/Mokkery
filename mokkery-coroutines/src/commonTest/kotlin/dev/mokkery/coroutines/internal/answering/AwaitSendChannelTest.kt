package dev.mokkery.coroutines.internal.answering

import dev.mokkery.answering.FunctionScope
import dev.mokkery.coroutines.fakeFunctionScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class AwaitSendChannelTest {

    private var elementDescription = "value=Unit"
    private val channel = Channel<Int>()
    private var element: suspend (FunctionScope) -> Int = { 1 }
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
            awaitable.await(fakeFunctionScope())
            element = { 2 }
            awaitable.await(fakeFunctionScope())
        }
        assertEquals(1, channel.receive())
        assertEquals(2, channel.receive())
    }

    @Test
    fun testPassesCorrectScopeToDeferredProvider() = runTest {
        backgroundScope.launch { channel.receive() }
        var passedScope: FunctionScope? = null
        element = { passedScope = it ; 1 }
        val scope = fakeFunctionScope()
        awaitable.await(scope)
        assertSame(scope, passedScope)
    }
}