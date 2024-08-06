package dev.mokkery.coroutines.internal.answering

import dev.mokkery.answering.FunctionScope
import dev.mokkery.coroutines.fakeFunctionScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class AwaitDelayedTest {

    private var value: suspend (FunctionScope) -> Int = { 1 }
    private var description: String = "value=1"
    private val awaitable = AwaitDelayed(
        duration = 1.seconds,
        valueDescription = { description },
        value = { value(it) }
    )

    @Test
    fun testReturnsProvidedValueAfterTheDelay() = runTest {
        val result = async { awaitable.await(fakeFunctionScope()) }
        advanceTimeBy(500)
        assertFalse(result.isCompleted)
        advanceTimeBy(501)
        assertTrue(result.isCompleted)
        assertEquals(1, result.await())
    }

    @Test
    fun testComposesProperDescription() {
        assertEquals("delayed(by=1s, value=1)", awaitable.description())
    }
}
