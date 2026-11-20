package dev.mokkery.test.args

import dev.mokkery.MokkeryRuntimeException
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.matcher.gt
import dev.mokkery.mock
import dev.mokkery.test.ComplexArgsInterface
import dev.mokkery.test.SuspendMethodsInterface
import dev.mokkery.verify
import dev.mokkery.verifySuspend
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ReturnsInArgumentsTest {

    private val mock = mock<ComplexArgsInterface>()

    @Test
    fun testReturnFromVerifyNotTaken() {
        val skip = false
        every { mock.callPrimitive(any()) } returns 1
        mock.callPrimitive(1)
        verify { mock.callPrimitive(if (skip) return@verify else 1) }
        assertFailsWith<AssertionError> { verify { mock.callPrimitive(if (skip) return@verify else 2) } }
    }

    @Test
    fun testReturnFromVerifyTaken() {
        val skip = true
        verify { mock.callPrimitive(if (skip) return@verify else 1) }
    }

    @Test
    fun testReturnFromEveryWithMatcherNotTaken() {
        val skip = false
        every { mock.callPrimitive(if (skip) return@every 0 else gt(1)) } returns 1
        assertEquals(1, mock.callPrimitive(2))
        assertFailsWith<MokkeryRuntimeException> { mock.callPrimitive(1) }
    }

    @Test
    fun testReturnInArgumentOfNonMockCall() {
        val skip = false
        every { mock.callPrimitive(any()) } returns 1
        mock.callPrimitive(2)
        verify { mock.callPrimitive(Doubler().double(if (skip) return@verify else 1)) }
    }

    @Test
    fun testReturnFromVerifySuspend() = runTest {
        val skip = false
        val mock = mock<SuspendMethodsInterface>()
        everySuspend { mock.callPrimitive(any()) } returns 1
        mock.callPrimitive(1)
        verifySuspend { mock.callPrimitive(if (skip) return@verifySuspend else 1) }
        assertFailsWith<AssertionError> { verifySuspend { mock.callPrimitive(if (skip) return@verifySuspend else 2) } }
    }

    @Test
    fun testSuspendCallInArgument() = runTest {
        val mock = mock<SuspendMethodsInterface>()
        everySuspend { mock.callPrimitive(any()) } returns 1
        mock.callPrimitive(1)
        verifySuspend { mock.callPrimitive(one()) }
    }

    private suspend fun one(): Int = 1

    private open class Doubler {
        open fun double(value: Int) = value * 2
    }
}
