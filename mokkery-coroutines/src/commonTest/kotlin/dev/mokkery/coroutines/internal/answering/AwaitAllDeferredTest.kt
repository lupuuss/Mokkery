package dev.mokkery.coroutines.internal.answering

import dev.mokkery.coroutines.await
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AwaitAllDeferredTest {

    private val awaitable = AwaitAllDeferred(listOf(CompletableDeferred(1), CompletableDeferred(2)))

    @Test
    fun testCreatesProperDescription() {
        val result = awaitable.description()
        assertTrue { result.startsWith("all(") }
        assertTrue { result.endsWith(")") }
        val nestedListStr = result.removePrefix("all(").removeSuffix(")")
        assertTrue { nestedListStr.split(", ").size == 2 }
    }

    @Test
    fun testReturnsDeferredResultsList() = runTest {
        assertEquals(listOf(1, 2), awaitable.await())
    }
}
