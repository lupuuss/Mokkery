package dev.mokkery.test

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.SuperCall.Companion.original
import dev.mokkery.answering.SuperCall.Companion.originalWith
import dev.mokkery.answering.SuperCall.Companion.superOf
import dev.mokkery.answering.SuperCall.Companion.superWith
import dev.mokkery.answering.calls
import dev.mokkery.answering.callsCatching
import dev.mokkery.answering.repeat
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.answering.returnsBy
import dev.mokkery.answering.returnsFailure
import dev.mokkery.answering.returnsFailureBy
import dev.mokkery.answering.returnsSuccess
import dev.mokkery.answering.returnsSuccessBy
import dev.mokkery.answering.self
import dev.mokkery.answering.sequentially
import dev.mokkery.answering.sequentiallyRepeat
import dev.mokkery.answering.sequentiallyReturns
import dev.mokkery.answering.sequentiallyThrows
import dev.mokkery.answering.throws
import dev.mokkery.answering.throwsBy
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class AnswersTest {

    private val mock = mock<TestInterface>()

    @Test
    fun testReturns() {
        every { mock.callGeneric(any<Int>()) } returns 3
        assertEquals(3, mock.callGeneric(0))
    }

    @Test
    fun testReturnsBy() {
        var value = 1
        every { mock.callWithDefault(1) } returnsBy { value++ }
        assertEquals(1, mock.callWithDefault(1))
        assertEquals(2, mock.callWithDefault(1))
    }

    @Test
    fun testReturnsSuccess() {
        every { mock.callWithPrimitiveResult(any()) } returnsSuccess 1
        assertEquals(Result.success(1), mock.callWithPrimitiveResult(Result.success(0)))
    }

    @Test
    fun testReturnsSuccessBy() {
        var value = 1
        every { mock.callWithPrimitiveResult(any()) } returnsSuccessBy { value++ }
        assertEquals(Result.success(1), mock.callWithPrimitiveResult(Result.success(0)))
        assertEquals(Result.success(2), mock.callWithPrimitiveResult(Result.success(0)))
    }

    @Test
    fun testReturnsFailure() {
        val error = IllegalArgumentException("Failed!")
        every { mock.callWithPrimitiveResult(any()) } returnsFailure error
        assertEquals(Result.failure(error), mock.callWithPrimitiveResult(Result.success(0)))
    }

    @Test
    fun testReturnsFailureBy() {
        every { mock.callWithPrimitiveResult(any()) } returnsFailureBy ::IllegalStateException
        val results = setOf(
            mock.callWithPrimitiveResult(Result.success(0)),
            mock.callWithPrimitiveResult(Result.success(0))
        )
        assertEquals(2, results.size)
        assertTrue { results.all { it.exceptionOrNull() is IllegalStateException } }
    }

    @Test
    fun testReturnsArgAt() {
        every { mock.callWithDefault(any()) } returnsArgAt 0
        assertEquals(33, mock.callWithDefault(33))
    }

    @Test
    fun testThrows() {
        every { mock.callGeneric(any<Int>()) } throws IllegalArgumentException()
        val error1 = assertFailsWith<IllegalArgumentException> { mock.callGeneric(0) }
        val error2 = assertFailsWith<IllegalArgumentException> { mock.callGeneric(0) }
        assertEquals(error1, error2)
    }

    @Test
    fun testThrowsBy() {
        every { mock.callWithDefault(1) } throwsBy ::IllegalStateException
        val error1 = assertFailsWith<IllegalStateException> { mock.callWithDefault(1) }
        val error2 = assertFailsWith<IllegalStateException> { mock.callWithDefault(1) }
        assertNotEquals(error1, error2)
    }

    @Test
    fun testCalls() {
        every { mock.callGeneric(any<Int>()) } calls { (i: Int) -> i + 1 }
        assertEquals(2, mock.callGeneric(1))
    }

    @Test
    fun testSuspendCalls() = runTest {
        everySuspend { mock.callWithSuspension(any()) } calls { (i: Int) -> listOf(i.toString()) }
        assertEquals(listOf("1"), mock.callWithSuspension(1))
    }

    @Test
    fun testCallsCatching() {
        every { mock.callWithPrimitiveResult(any()) } callsCatching { (result: Result<Int>) -> result.getOrThrow() }
        assertEquals(1, mock.callWithPrimitiveResult(Result.success(1)).getOrNull())
        val exception = IllegalStateException()
        assertEquals(exception, mock.callWithPrimitiveResult(Result.failure(exception)).exceptionOrNull())
        assertFailsWith<MokkeryRuntimeException> {
            mock.callWithPrimitiveResult(Result.failure(MokkeryRuntimeException()))
        }
    }

    @Test
    fun testSuspendCallsCatching() = runTest {
        everySuspend { mock.callWithResult(any<Result<Int>>()) } callsCatching { (result: Result<Int>) ->
            result.getOrThrow()
        }
        assertEquals(1, mock.callWithResult(Result.success(1)).getOrNull())
        val exception = IllegalStateException()
        assertEquals(exception, mock.callWithResult(Result.failure<Int>(exception)).exceptionOrNull())
        assertFailsWith<MokkeryRuntimeException> {
            mock.callWithResult(Result.failure<Int>(MokkeryRuntimeException()))
        }
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
            calls { listOf("2") }
            throws(IllegalArgumentException())
        }
        assertEquals(listOf("1"), mock.callWithSuspension(0))
        assertEquals(listOf("2"), mock.callWithSuspension(0))
        assertFailsWith<IllegalArgumentException> { mock.callWithSuspension(0) }
        assertFailsWith<MokkeryRuntimeException> { mock.callWithSuspension(0) }
    }

    @Test
    fun testSequentiallyWithRepeat() {
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
    fun testSequentiallySuspendWithRepeat() = runTest {
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
    fun testSequentiallyRepeat() {
        every { mock.callGeneric(any<Int>()) } sequentiallyRepeat {
            returns(1)
            returns(2)
        }
        assertEquals(1, mock.callGeneric(0))
        assertEquals(2, mock.callGeneric(0))
        assertEquals(1, mock.callGeneric(0))
        assertEquals(2, mock.callGeneric(0))
    }

    @Test
    fun testSuspendSequentiallyRepeat() = runTest {
        everySuspend { mock.callWithSuspension(any()) } sequentiallyRepeat {
            returns(listOf("1"))
            returns(listOf("2"))
        }
        assertEquals(listOf("1"), mock.callWithSuspension(0))
        assertEquals(listOf("2"), mock.callWithSuspension(0))
        assertEquals(listOf("1"), mock.callWithSuspension(0))
        assertEquals(listOf("2"), mock.callWithSuspension(0))
    }

    @Test
    fun testSequentiallyReturns() {
        every { mock.callGeneric(any<Int>()) } sequentiallyReturns listOf(1, 2, 3)
        assertEquals(1, mock.callGeneric(0))
        assertEquals(2, mock.callGeneric(0))
        assertEquals(3, mock.callGeneric(0))
        assertFailsWith<MokkeryRuntimeException> { mock.callGeneric(0) }
    }

    @Test
    fun testSequentiallyThrows() {
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
    fun testCallsOriginalWithoutDirectOverride() {
        every { mock.callWithDefaultNoOverride() } calls original
        assertEquals(10, mock.callWithDefaultNoOverride())
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
        mock.callUnit()
    }

    @Test
    fun testSelfHasCorrectTypeForFunctionalType() {
        val funMock = mock<(Any) -> Unit>()
        every { funMock(any()) } calls {
            assertIs<(Any) -> Unit>(self<(Any) -> Unit>())
        }
        funMock(Unit)
    }
}
