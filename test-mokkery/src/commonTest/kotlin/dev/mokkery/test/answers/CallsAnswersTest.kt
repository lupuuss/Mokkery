package dev.mokkery.test.answers

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.calls
import dev.mokkery.answering.callsCatching
import dev.mokkery.answering.self
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
import kotlin.test.assertIs

class CallsAnswersTest {

    private val regularMock = mock<RegularMethodsInterface>()
    private val suspendMock = mock<SuspendMethodsInterface>()


    @Test
    fun testCalls() {
        every { regularMock.callPrimitive(any()) } calls { (i: Int) -> i + 1 }
        assertEquals(2, regularMock.callPrimitive(1))
    }

    @Test
    fun testSuspendCalls() = runTest {
        everySuspend { suspendMock.callPrimitive(any()) } calls { (i: Int) -> i + 1 }
        assertEquals(2, suspendMock.callPrimitive(1))
    }


    @Test
    fun testCallsCatching() {
        every { regularMock.callResult(any()) } callsCatching { (result: Result<Int>) -> result.getOrThrow() }
        assertEquals(1, regularMock.callResult(Result.success(1)).getOrNull())
        val exception = IllegalStateException()
        assertEquals(exception, regularMock.callResult(Result.failure(exception)).exceptionOrNull())
        assertFailsWith<MokkeryRuntimeException> {
            regularMock.callResult(Result.failure(MokkeryRuntimeException()))
        }
    }

    @Test
    fun testSuspendCallsCatching() = runTest {
        everySuspend { suspendMock.callResult(any()) } callsCatching { (result: Result<Int>) ->
            result.getOrThrow()
        }
        assertEquals(1, suspendMock.callResult(Result.success(1)).getOrNull())
        val exception = IllegalStateException()
        assertEquals(exception, suspendMock.callResult(Result.failure<Int>(exception)).exceptionOrNull())
        assertFailsWith<MokkeryRuntimeException> {
            suspendMock.callResult(Result.failure<Int>(MokkeryRuntimeException()))
        }
    }

    @Test
    fun testSelfHasCorrectType() {
        every { regularMock.callUnit(Unit) } calls {
            assertIs<RegularMethodsInterface>(self<RegularMethodsInterface>())
        }
        regularMock.callUnit(Unit)
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
