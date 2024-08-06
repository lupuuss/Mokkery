package dev.mokkery.coroutines.internal.answering

import dev.mokkery.answering.FunctionScope
import dev.mokkery.coroutines.TestAwaitable
import dev.mokkery.coroutines.fakeFunctionScope
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
        val scope = fakeFunctionScope()
        assertEquals(1, answer.callSuspend(scope))
        assertEquals(2, answer.callSuspend(scope))
    }

    @Test
    fun testPassesCorrectFunctionScopeToAwaitable() = runTest {
        var passedScope: FunctionScope? = null
        awaitable.await = { passedScope = it; awaitValue }
        val scope = fakeFunctionScope()
        answer.callSuspend(scope)
        assertSame(scope, passedScope)
    }

    @Test
    fun testFailsOnRegularCall() {
        assertFails { answer.call(fakeFunctionScope()) }
    }
}
