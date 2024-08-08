package dev.mokkery.test

import dev.mokkery.coroutines.answering.Awaitable.Companion.all
import dev.mokkery.coroutines.answering.Awaitable.Companion.cancellation
import dev.mokkery.coroutines.answering.Awaitable.Companion.delayed
import dev.mokkery.coroutines.answering.Awaitable.Companion.receive
import dev.mokkery.coroutines.answering.Awaitable.Companion.send
import dev.mokkery.coroutines.answering.awaits
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutinesAnsweringTest {

    private val mock = mock<TestInterface>()


    @Test
    fun testAwaitsCancellation() = runTest {
        everySuspend { mock.fetchWithDefault(any()) } awaits cancellation
        var cancelled = false
        val job = launch {
            try {
                mock.fetchWithDefault(0)
            } catch (e: CancellationException) {
                cancelled = true
            }
        }
        advanceUntilIdle()
        assertFalse(cancelled)
        job.cancel()
        advanceUntilIdle()
        assertTrue(cancelled)
    }

    @Test
    fun testAwaitsDeferred() = runTest {
        everySuspend { mock.fetchWithDefault(any()) } awaits CompletableDeferred(1)
        assertEquals(1, mock.fetchWithDefault(0))
    }

    @Test
    fun testAwaitsProvidedDeferred() = runTest {
        everySuspend { mock.fetchWithDefault(any()) } awaits { (i: Int) -> CompletableDeferred(i) }
        assertEquals(0, mock.fetchWithDefault(0))
        assertEquals(1, mock.fetchWithDefault(1))
    }

    @Test
    fun testAwaitsAllDeferred() = runTest {
        val deferreds = listOf(CompletableDeferred("1"), CompletableDeferred("2"))
        everySuspend { mock.callWithSuspension(any()) } awaits all(deferreds)
        assertEquals(listOf("1", "2"), mock.callWithSuspension(0))
    }

    @Test
    fun testAwaitsReceiveFromChannel() = runTest {
        val channel = Channel<Int>()
        backgroundScope.launch { channel.send(1) }
        everySuspend { mock.fetchWithDefault(any()) } awaits receive(from = channel)
        assertEquals(1, mock.fetchWithDefault(0))
    }

    @Test
    fun testAwaitsProvidedValueSendToChannel() = runTest {
        val channel = Channel<Int>()
        var value = 2
        everySuspend { mock.callUnitWithSuspension(any()) } awaits send(to = channel) { value++ }
        backgroundScope.launch {
            mock.callUnitWithSuspension(1)
            mock.callUnitWithSuspension(2)
        }
        assertEquals(2, channel.receive())
        assertEquals(3, channel.receive())
    }

    @Test
    fun testAwaitsDelayedValue() = runTest {
        everySuspend { mock.callWithSuspension(1) } awaits delayed(listOf("1"), by = 3.seconds)
        val result = async { mock.callWithSuspension(1) }
        advanceTimeBy(2.seconds)
        assertFalse(result.isCompleted)
        advanceTimeBy(1.seconds + 1.milliseconds)
        assertTrue(result.isCompleted)
        assertEquals(listOf("1"), result.await())
    }

    @Test
    fun testAwaitsDelayedProvidedValue() = runTest {
        everySuspend { mock.callWithSuspension(any()) } awaits delayed(by = 3.seconds) { (i: Int) -> listOf(i.toString()) }
        val result1 = async { mock.callWithSuspension(1) }
        val result2 = async { mock.callWithSuspension(2) }
        advanceTimeBy(2.seconds)
        assertFalse(result1.isCompleted)
        assertFalse(result2.isCompleted)
        advanceTimeBy(1.seconds + 1.milliseconds)
        assertTrue(result1.isCompleted)
        assertTrue(result2.isCompleted)
        assertEquals(listOf("1"), result1.await())
        assertEquals(listOf("2"), result2.await())
    }

    @Test
    fun testAwaitsDelayedUnit() = runTest {
        everySuspend { mock.callUnitWithSuspension(1) } awaits delayed(by = 3.seconds)
        val result = async { mock.callUnitWithSuspension(1) }
        advanceTimeBy(2.seconds)
        assertFalse(result.isCompleted)
        advanceTimeBy(1.seconds + 1.milliseconds)
        assertTrue(result.isCompleted)
        assertEquals(Unit, result.await())
    }
}
