package dev.mokkery.coroutines.internal.answering

import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.coroutines.TestAwaitable
import dev.mokkery.coroutines.createMokkeryBlockingCallScope
import dev.mokkery.coroutines.createMokkerySuspendCallScope
import dev.mokkery.coroutines.answering.Awaitable
import dev.mokkery.coroutines.testRendering
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
    fun testRendersLegacyAwaitableDescription() {
        // an `Awaitable` written against 3.4.x only overrides the deprecated `description()`
        val legacy = object : Awaitable<Int> {
            override suspend fun await(scope: MokkerySuspendCallScope): Int = 1
            override fun description(): String = "legacyDescription()"
        }
        assertEquals("awaits legacyDescription()", testRendering { AwaitAnswer(legacy).render() })
    }

    @Test
    fun testFailsOnRegularCall() {
        assertFails { answer.call(createMokkeryBlockingCallScope()) }
    }
}
