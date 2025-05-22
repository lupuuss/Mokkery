package dev.mokkery.coroutines.internal.answering

import dev.mokkery.coroutines.await
import dev.mokkery.coroutines.createMokkerySuspendCallScope
import dev.mokkery.MokkerySuspendCallScope
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class AwaitDeferredTest {

    private var description = "description"
    private var deferred: (MokkerySuspendCallScope) -> Deferred<Int> = { CompletableDeferred(1) }
    private val awaitable = AwaitDeferred(
        description = { description },
        deferred = { deferred(it) }
    )

    @Test
    fun testCallsDescriptionOnEachCall() {
        assertEquals("description", awaitable.description())
        description = "new description"
        assertEquals("new description", awaitable.description())
    }

    @Test
    fun testAwaitsProvidedDeferredOnEachCall() = runTest {
        assertEquals(1, awaitable.await())
        deferred = { CompletableDeferred(2) }
        assertEquals(2, awaitable.await())
        deferred = { CompletableDeferred(3) }
        assertEquals(3, awaitable.await())
    }

    @Test
    fun testPassesCorrectScopeToDeferredProvider() = runTest {
        var passedScope: MokkerySuspendCallScope? = null
        deferred = { passedScope = it; CompletableDeferred(1) }
        val scope = createMokkerySuspendCallScope()
        awaitable.await(scope)
        assertSame(scope, passedScope)
    }
}
