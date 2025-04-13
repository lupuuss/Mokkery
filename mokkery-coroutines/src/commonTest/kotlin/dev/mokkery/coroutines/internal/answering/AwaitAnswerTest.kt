package dev.mokkery.coroutines.internal.answering

import dev.mokkery.coroutines.TestAwaitable
import dev.mokkery.coroutines.createMokkeryBlockingCallScope
import dev.mokkery.coroutines.createMokkerySuspendCallScope
import dev.mokkery.interceptor.MokkerySuspendCallScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertSame

class AwaitAnswerTest {

    private var awaitValue = 1
    private val awaitable = TestAwaitable(await = { awaitValue++ }, desc = { "description($awaitValue)" })
    private val answer = AwaitAnswer(awaitable = awaitable)

    @Test
    fun testComposesProperDescriptionOnEachCall() {
        assertEquals("awaits description(1)", answer.description())
        awaitValue++
        assertEquals("awaits description(2)", answer.description())
    }


    @Test
    fun testCallsAwaitOnEachCall() = runTest {
        assertEquals(1, answer.call(createMokkerySuspendCallScope()))
        assertEquals(2, answer.call(createMokkerySuspendCallScope()))
    }

    @Test
    fun testPassesCorrectFunctionScopeToAwaitable() = runTest {
        var passedScope: MokkerySuspendCallScope? = null
        awaitable.await = { passedScope = it; awaitValue }
        val scope = createMokkerySuspendCallScope()
        answer.call(scope)
        assertSame(scope, passedScope)
    }

    @Test
    fun testFailsOnRegularCall() {
        assertFails { answer.call(createMokkeryBlockingCallScope()) }
    }
}
