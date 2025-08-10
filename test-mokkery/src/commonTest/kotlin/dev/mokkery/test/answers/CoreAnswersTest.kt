package dev.mokkery.test.answers

import dev.mokkery.MokkeryBlockingCallScope
import dev.mokkery.MokkerySuspendCallScope
import dev.mokkery.annotations.DelicateMokkeryApi
import dev.mokkery.answering.Answer
import dev.mokkery.answering.returns
import dev.mokkery.answering.returnsArgAt
import dev.mokkery.answering.returnsBy
import dev.mokkery.answering.returnsFailure
import dev.mokkery.answering.returnsFailureBy
import dev.mokkery.answering.returnsSuccess
import dev.mokkery.answering.returnsSuccessBy
import dev.mokkery.answering.throws
import dev.mokkery.answering.throwsBy
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.test.RegularMethodsInterface
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class CoreAnswersTest {

    private val mock = mock<RegularMethodsInterface>()

    @Test
    fun testReturns() {
        every { mock.callPrimitive(any()) } returns 3
        assertEquals(3, mock.callPrimitive(1))
    }

    @Test
    fun testReturnsBy() {
        var value = 1
        every { mock.callPrimitive(1) } returnsBy { value++ }
        assertEquals(1, mock.callPrimitive(1))
        assertEquals(2, mock.callPrimitive(1))
    }

    @Test
    fun testReturnsSuccess() {
        every { mock.callResult(any()) } returnsSuccess 1
        assertEquals(Result.success(1), mock.callResult(Result.success(0)))
    }

    @Test
    fun testReturnsSuccessBy() {
        var value = 1
        every { mock.callResult(any()) } returnsSuccessBy { value++ }
        assertEquals(Result.success(1), mock.callResult(Result.success(0)))
        assertEquals(Result.success(2), mock.callResult(Result.success(0)))
    }

    @Test
    fun testReturnsFailure() {
        val error = IllegalArgumentException("Failed!")
        every { mock.callResult(any()) } returnsFailure error
        assertEquals(Result.failure(error), mock.callResult(Result.success(0)))
    }

    @Test
    fun testReturnsFailureBy() {
        every { mock.callResult(any()) } returnsFailureBy ::IllegalStateException
        val results = setOf(
            mock.callResult(Result.success(0)),
            mock.callResult(Result.success(0))
        )
        assertEquals(2, results.size)
        assertTrue { results.all { it.exceptionOrNull() is IllegalStateException } }
    }

    @Test
    fun testReturnsArgAt() {
        every { mock.callPrimitive(any()) } returnsArgAt 0
        assertEquals(33, mock.callPrimitive(33))
    }


    @Test
    fun testThrows() {
        every { mock.callPrimitive(any()) } throws IllegalArgumentException()
        val error1 = assertFailsWith<IllegalArgumentException> { mock.callPrimitive(0) }
        val error2 = assertFailsWith<IllegalArgumentException> { mock.callPrimitive(0) }
        assertEquals(error1, error2)
    }

    @Test
    fun testThrowsBy() {
        every { mock.callPrimitive(1) } throwsBy ::IllegalStateException
        val error1 = assertFailsWith<IllegalStateException> { mock.callPrimitive(1) }
        val error2 = assertFailsWith<IllegalStateException> { mock.callPrimitive(1) }
        assertNotEquals(error1, error2)
    }

    @Test
    fun testCustomAnswer() {

    }

}

@OptIn(DelicateMokkeryApi::class)
private data class CustomAnswer<T>(val value: T) : Answer<T> {

    override fun call(scope: MokkeryBlockingCallScope): T = value

    override suspend fun call(scope: MokkerySuspendCallScope): T = value
}
