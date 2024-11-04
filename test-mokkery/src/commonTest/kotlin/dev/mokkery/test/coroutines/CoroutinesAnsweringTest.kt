package dev.mokkery.test.coroutines

import dev.mokkery.answering.calls
import dev.mokkery.coroutines.answering.Awaitable.Companion.all
import dev.mokkery.coroutines.answering.Awaitable.Companion.cancellation
import dev.mokkery.coroutines.answering.Awaitable.Companion.delayed
import dev.mokkery.coroutines.answering.Awaitable.Companion.receive
import dev.mokkery.coroutines.answering.Awaitable.Companion.send
import dev.mokkery.coroutines.answering.awaits
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.ComplexType
import dev.mokkery.test.SuspendMethodsInterface
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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

    private val mock = mock<SuspendMethodsInterface>()

    @Test
    fun testSuspendCalls() = runTest {
        everySuspend { mock.callPrimitive(any()) } calls { (i: Int) ->
            delay(3_000)
            i + 1
        }
        val result = async { mock.callPrimitive(1) }
        advanceTimeBy(2_000)
        assertFalse(result.isCompleted)
        advanceTimeBy(2_000)
        assertTrue(result.isCompleted)
        assertEquals(2, result.await())
    }

    @Test
    fun testSuspendCallsForFunctionalTypes() = runTest {
        val funMock = mock<suspend (Int) -> Int> {
            everySuspend { invoke(any()) } calls { (i: Int) ->
                delay(3_000)
                i + 1
            }
        }
        val result = async { funMock(1) }
        advanceTimeBy(2_000)
        assertFalse(result.isCompleted)
        advanceTimeBy(2_000)
        assertTrue(result.isCompleted)
        assertEquals(2, result.await())
    }

    @Test
    fun testAwaitsCancellation() = runTest {
        everySuspend { mock.callComplex(any()) } awaits cancellation
        var cancelled = false
        val job = launch {
            try {
                mock.callComplex(ComplexType.Companion)
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
        everySuspend { mock.callPrimitive(any()) } awaits CompletableDeferred(1)
        assertEquals(1, mock.callPrimitive(0))
    }

    @Test
    fun testAwaitsProvidedDeferred() = runTest {
        everySuspend { mock.callPrimitive(any()) } awaits { (i: Int) -> CompletableDeferred(i) }
        assertEquals(0, mock.callPrimitive(0))
        assertEquals(1, mock.callPrimitive(1))
    }

    @Test
    fun testAwaitsAllDeferred() = runTest {
        val funMock = mock<suspend (i: Int) -> List<Int>>()
        val deferreds = listOf(CompletableDeferred(1), CompletableDeferred(2))
        everySuspend { funMock(any()) } awaits all(deferreds)
        assertEquals(listOf(1, 2), funMock(0))
    }

    @Test
    fun testAwaitsReceiveFromChannel() = runTest {
        val channel = Channel<Int>()
        backgroundScope.launch { channel.send(1) }
        everySuspend { mock.callPrimitive(any()) } awaits receive(from = channel)
        assertEquals(1, mock.callPrimitive(0))
    }

    @Test
    fun testAwaitsProvidedValueSendToChannel() = runTest {
        val channel = Channel<Int>()
        var value = 2
        val funMock = mock<suspend (i: Int) -> Unit>()
        everySuspend { funMock(any()) } awaits send(to = channel) { value++ }
        backgroundScope.launch {
            funMock(1)
            funMock(2)
        }
        assertEquals(2, channel.receive())
        assertEquals(3, channel.receive())
    }

    @Test
    fun testAwaitsDelayedValue() = runTest {
        everySuspend { mock.callPrimitive(1) } awaits delayed(1, by = 3.seconds)
        val result = async { mock.callPrimitive(1) }
        advanceTimeBy(2.seconds)
        assertFalse(result.isCompleted)
        advanceTimeBy(1.seconds + 1.milliseconds)
        assertTrue(result.isCompleted)
        assertEquals(1, result.await())
    }

    @Test
    fun testAwaitsDelayedProvidedValue() = runTest {
        everySuspend { mock.callPrimitive(any()) } awaits delayed(by = 3.seconds) { (i: Int) -> i }
        val result1 = async { mock.callPrimitive(1) }
        val result2 = async { mock.callPrimitive(2) }
        advanceTimeBy(2.seconds)
        assertFalse(result1.isCompleted)
        assertFalse(result2.isCompleted)
        advanceTimeBy(1.seconds + 1.milliseconds)
        assertTrue(result1.isCompleted)
        assertTrue(result2.isCompleted)
        assertEquals(1, result1.await())
        assertEquals(2, result2.await())
    }

    @Test
    fun testAwaitsDelayedUnit() = runTest {
        everySuspend { mock.callUnit(Unit) } awaits delayed(by = 3.seconds)
        val result = async { mock.callUnit(Unit) }
        advanceTimeBy(2.seconds)
        assertFalse(result.isCompleted)
        advanceTimeBy(1.seconds + 1.milliseconds)
        assertTrue(result.isCompleted)
        assertEquals(Unit, result.await())
    }
}
