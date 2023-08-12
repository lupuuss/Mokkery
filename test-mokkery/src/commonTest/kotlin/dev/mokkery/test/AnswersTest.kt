package dev.mokkery.test

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.SuperCall.Companion.original
import dev.mokkery.answering.SuperCall.Companion.originalWith
import dev.mokkery.answering.SuperCall.Companion.superOf
import dev.mokkery.answering.SuperCall.Companion.superWith
import dev.mokkery.answering.calls
import dev.mokkery.answering.repeat
import dev.mokkery.answering.returns
import dev.mokkery.answering.self
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
import kotlin.test.assertIs

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

    @Test
    fun testCallsOriginalFromScope() {
        every { mock.callWithDefault(any()) } calls {
            callOriginal()
        }
        assertEquals(3, mock.callWithDefault(1))
    }

    @Test
    fun testCallsOriginalSuspendFromScope() = runTest {
        everySuspend { mock.fetchWithDefault(any()) } calls {
            callOriginal()
        }
        assertEquals(3, mock.fetchWithDefault(1))
    }

    @Test
    fun testCallsOriginalWithArgsFromScope() {
        every { mock.callWithDefault(any()) } calls {
            callOriginalWith(2)
        }
        assertEquals(4, mock.callWithDefault(1))
    }

    @Test
    fun testCallsOriginalWithArgsSuspendFromScope() = runTest {
        everySuspend { mock.fetchWithDefault(any()) } calls {
            callOriginalWith(2)
        }
        assertEquals(4, mock.fetchWithDefault(1))
    }


    @Test
    fun testCallsSuperFromScope() {
        every { mock.callWithDefault(any()) } calls {
            callSuper(BaseInterface::class)
        }
        assertEquals(2, mock.callWithDefault(1))
    }

    @Test
    fun testCallsSuperSuspendFromScope() = runTest {
        everySuspend { mock.fetchWithDefault(any()) } calls {
            callSuper(BaseInterface::class)
        }
        assertEquals(2, mock.fetchWithDefault(1))
    }

    @Test
    fun testCallsSuperWithArgsFromScope() {
        every { mock.callWithDefault(any()) } calls {
            callSuperWith(BaseInterface::class, 2)
        }
        assertEquals(3, mock.callWithDefault(1))
    }

    @Test
    fun testCallsSuperWithArgsSuspendFromScope() = runTest {
        everySuspend { mock.fetchWithDefault(any()) } calls {
            callSuperWith(BaseInterface::class, 2)
        }
        assertEquals(3, mock.fetchWithDefault(1))
    }

    @Test
    fun testCallsOriginal() {
        every { mock.callWithDefault(any()) } calls original
        assertEquals(3, mock.callWithDefault(1))
    }

    @Test
    fun testCallsOriginalSuspend() = runTest {
        everySuspend { mock.fetchWithDefault(any()) } calls original
        assertEquals(3, mock.fetchWithDefault(1))
    }

    @Test
    fun testCallsOriginalWithArgs() {
        every { mock.callWithDefault(any()) } calls originalWith(2)
        assertEquals(4, mock.callWithDefault(1))
    }

    @Test
    fun testCallsOriginalWithArgsSuspend() = runTest {
        everySuspend { mock.fetchWithDefault(any()) } calls originalWith(2)
        assertEquals(4, mock.fetchWithDefault(1))
    }


    @Test
    fun testCallsSuper() {
        every { mock.callWithDefault(any()) } calls superOf<BaseInterface>()
        assertEquals(2, mock.callWithDefault(1))
    }

    @Test
    fun testCallsSuperSuspend() = runTest {
        everySuspend { mock.fetchWithDefault(any()) } calls superOf<BaseInterface>()
        assertEquals(2, mock.fetchWithDefault(1))
    }

    @Test
    fun testCallsSuperWithArgs() {
        every { mock.callWithDefault(any()) } calls superWith<BaseInterface>(2)
        assertEquals(3, mock.callWithDefault(1))
    }

    @Test
    fun testCallsSuperWithArgsSuspend() = runTest {
        everySuspend { mock.fetchWithDefault(any()) } calls superWith<BaseInterface>(2)
        assertEquals(3, mock.fetchWithDefault(1))
    }

    @Test
    fun testSelfHasCorrectType() {
        every { mock.callUnit() } calls {
            assertIs<TestInterface>(self<TestInterface>())
        }
    }

    @Test
    fun testSelfHasCorrectTypeForFunctionalType() {
        val funMock = mock<(Any) -> Unit>()
        every { funMock(any()) } calls {
            assertIs<(Any) -> Unit>(self<(Any) -> Unit>())
        }
    }
}
