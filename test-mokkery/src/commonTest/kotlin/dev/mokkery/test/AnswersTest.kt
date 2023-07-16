package dev.mokkery.test

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.calls
import dev.mokkery.answering.repeat
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.answering.sequentiallyThrows
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AnswersTest {

    private val mock = mock<TestInterface>()

    @Test
    fun testReturns() {
        every { mock.callGeneric(any<Int>()) } returns 3
        assertEquals(3, mock.callGeneric(0))
    }

    @Test
    fun testThrows() {
        every { mock.callGeneric(any<Int>()) } throws IllegalArgumentException()
        assertFailsWith<IllegalArgumentException> {
            mock.callGeneric(0)
        }
    }

    @Test
    fun testCalls() {
        every { mock.callGeneric(any<Int>()) } calls { (i: Int) -> i + 1 }
        assertEquals(2, mock.callGeneric(1))
    }

    @Test
    fun testSuspendCalls() = runTest {
        everySuspend { mock.callWithSuspension(any()) } calls { (i: Int) ->
            delay(1)
            listOf(i.toString())
        }
        assertEquals(listOf("1"), mock.callWithSuspension(1))
    }

    @Test
    fun testSequentially() {
        every { mock.callGeneric(any<Int>()) } sequentially {
            returns(1)
            calls { 2 }
            throws(IllegalArgumentException())
        }
        assertEquals(1, mock.callGeneric(0))
        assertEquals(2, mock.callGeneric(0))
        assertFailsWith<IllegalArgumentException> { mock.callGeneric(0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callGeneric(0) }
    }

    @Test
    fun testSequentiallySuspend() = runTest {
        everySuspend { mock.callWithSuspension(any()) } sequentially {
            returns(listOf("1"))
            calls {
                delay(1)
                listOf("2")
            }
            throws(IllegalArgumentException())
        }
        assertEquals(listOf("1"), mock.callWithSuspension(0))
        assertEquals(listOf("2"), mock.callWithSuspension(0))
        assertFailsWith<IllegalArgumentException> { mock.callWithSuspension(0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithSuspension(0) }
    }

    @Test
    fun testSequentiallyRepeat() {
        every { mock.callGeneric(any<Int>()) } sequentially {
            returns(1)
            returns(2)
            repeat {
                returns(3)
                returns(4)
            }
        }
        assertEquals(1, mock.callGeneric(0))
        assertEquals(2, mock.callGeneric(0))
        assertEquals(3, mock.callGeneric(0))
        assertEquals(4, mock.callGeneric(0))
        assertEquals(3, mock.callGeneric(0))
        assertEquals(4, mock.callGeneric(0))
    }

    @Test
    fun testSequentiallySuspendRepeat() = runTest {
        everySuspend { mock.callWithSuspension(any()) } sequentially {
            returns(listOf("1"))
            returns(listOf("2"))
            repeat {
                returns(listOf("3"))
                returns(listOf("4"))
            }
        }
        assertEquals(listOf("1"), mock.callWithSuspension(0))
        assertEquals(listOf("2"), mock.callWithSuspension(0))
        assertEquals(listOf("3"), mock.callWithSuspension(0))
        assertEquals(listOf("4"), mock.callWithSuspension(0))
        assertEquals(listOf("3"), mock.callWithSuspension(0))
        assertEquals(listOf("4"), mock.callWithSuspension(0))
    }

    @Test
    fun testSequentiallyReturns() = runTest {
        every { mock.callGeneric(any<Int>()) } sequentiallyReturns listOf(1, 2, 3)
        assertEquals(1, mock.callGeneric(0))
        assertEquals(2, mock.callGeneric(0))
        assertEquals(3, mock.callGeneric(0))
        assertFailsWith<MokkeryRuntimeException> { mock.callGeneric(0) }
    }

    @Test
    fun testSequentiallyThrows() = runTest {
        every { mock.callGeneric(any<Int>()) } sequentiallyThrows listOf(
            IllegalArgumentException(),
            IndexOutOfBoundsException(),
            IllegalStateException(),
        )
        assertFailsWith<IllegalArgumentException> { mock.callGeneric(0) }
        assertFailsWith<IndexOutOfBoundsException> { mock.callGeneric(0) }
        assertFailsWith<IllegalStateException> { mock.callGeneric(0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callGeneric(0) }
    }
}
