package dev.mokkery.test.answers

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.calls
import dev.mokkery.answering.repeat
import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.answering.sequentiallyRepeat
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.answering.sequentiallyThrows
import dev.mokkery.answering.throws
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.RegularMethodsInterface
import dev.mokkery.test.SuspendMethodsInterface
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SequentiallyAnswersTest {

    private val regularMock = mock<RegularMethodsInterface>()
    private val suspendMock = mock<SuspendMethodsInterface>()


    @Test
    fun testSequentially() {
        every { regularMock.callPrimitive(any()) } sequentially {
            returns(1)
            calls { 2 }
            throws(IllegalArgumentException())
        }
        assertEquals(1, regularMock.callPrimitive(0))
        assertEquals(2, regularMock.callPrimitive(0))
        assertFailsWith<IllegalArgumentException> { regularMock.callPrimitive(0) }
        assertFailsWith<MokkeryRuntimeException> { regularMock.callPrimitive(0) }
    }

    @Test
    fun testSequentiallySuspend() = runTest {
        everySuspend { suspendMock.callPrimitive(any()) } sequentially {
            returns(1)
            calls { 2 }
            throws(IllegalArgumentException())
        }
        assertEquals(1, suspendMock.callPrimitive(0))
        assertEquals(2, suspendMock.callPrimitive(0))
        assertFailsWith<IllegalArgumentException> { suspendMock.callPrimitive(0) }
        assertFailsWith<MokkeryRuntimeException> { suspendMock.callPrimitive(0) }
    }

    @Test
    fun testSequentiallyWithRepeat() {
        every { regularMock.callPrimitive(any()) } sequentially {
            returns(1)
            returns(2)
            repeat {
                returns(3)
                returns(4)
            }
        }
        assertEquals(1, regularMock.callPrimitive(0))
        assertEquals(2, regularMock.callPrimitive(0))
        assertEquals(3, regularMock.callPrimitive(0))
        assertEquals(4, regularMock.callPrimitive(0))
        assertEquals(3, regularMock.callPrimitive(0))
        assertEquals(4, regularMock.callPrimitive(0))
    }

    @Test
    fun testSequentiallySuspendWithRepeat() = runTest {
        everySuspend { suspendMock.callPrimitive(any()) } sequentially {
            returns(1)
            returns(2)
            repeat {
                returns(3)
                returns(4)
            }
        }
        assertEquals(1, suspendMock.callPrimitive(0))
        assertEquals(2, suspendMock.callPrimitive(0))
        assertEquals(3, suspendMock.callPrimitive(0))
        assertEquals(4, suspendMock.callPrimitive(0))
        assertEquals(3, suspendMock.callPrimitive(0))
        assertEquals(4, suspendMock.callPrimitive(0))
    }

    @Test
    fun testSequentiallyRepeat() {
        every { regularMock.callPrimitive(any()) } sequentiallyRepeat {
            returns(1)
            returns(2)
        }
        assertEquals(1, regularMock.callPrimitive(0))
        assertEquals(2, regularMock.callPrimitive(0))
        assertEquals(1, regularMock.callPrimitive(0))
        assertEquals(2, regularMock.callPrimitive(0))
    }

    @Test
    fun testSuspendSequentiallyRepeat() = runTest {
        everySuspend { suspendMock.callPrimitive(any()) } sequentiallyRepeat {
            returns(1)
            returns(2)
        }
        assertEquals(1, suspendMock.callPrimitive(0))
        assertEquals(2, suspendMock.callPrimitive(0))
        assertEquals(1, suspendMock.callPrimitive(0))
        assertEquals(2, suspendMock.callPrimitive(0))
    }

    @Test
    fun testSequentiallyReturns() {
        every { regularMock.callPrimitive(any()) } sequentiallyReturns listOf(1, 2, 3)
        assertEquals(1, regularMock.callPrimitive(0))
        assertEquals(2, regularMock.callPrimitive(0))
        assertEquals(3, regularMock.callPrimitive(0))
        assertFailsWith<MokkeryRuntimeException> { regularMock.callPrimitive(0) }
    }

    @Test
    fun testSequentiallyThrows() {
        every { regularMock.callPrimitive(any()) } sequentiallyThrows listOf(
            IllegalArgumentException(),
            IndexOutOfBoundsException(),
            IllegalStateException(),
        )
        assertFailsWith<IllegalArgumentException> { regularMock.callPrimitive(0) }
        assertFailsWith<IndexOutOfBoundsException> { regularMock.callPrimitive(0) }
        assertFailsWith<IllegalStateException> { regularMock.callPrimitive(0) }
        assertFailsWith<MokkeryRuntimeException> { regularMock.callPrimitive(0) }
    }

}
