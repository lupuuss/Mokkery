package dev.mokkery.internal.tracing

import dev.mokkery.internal.Counter
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallContext
import dev.mokkery.test.fakeCallTrace
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CallTracingInterceptorTest {
    private var clockStamp = 0L
    private val clock = Counter { clockStamp }
    private val tracing = CallTracingInterceptor(clock)
    private val args1 = listOf(
        fakeCallArg(name = "i", value = 1),
        fakeCallArg(name = "j", value = 1),
    )
    private val args2 = listOf(
        fakeCallArg(name = "firstName", value = "John"),
        fakeCallArg(name = "lastName", value = "Doe"),
    )

    @Test
    fun testRegistersCallsOnInterceptCall() {
        tracing.interceptCall(fakeCallContext<Int>(name = "call", args = args1))
        tracing.interceptCall(fakeCallContext<Int>(name = "call", args = args2))
        val expected = listOf(
            fakeCallTrace(name = "call", args = args1),
            fakeCallTrace(name = "call", args = args2),
        )
        assertEquals(expected, tracing.all)
        assertEquals(expected, tracing.unverified)
    }

    @Test
    fun testRegistersCallsOnInterceptSuspendCall() = runTest {
        tracing.interceptSuspendCall(fakeCallContext<Int>(name = "call", args = args1))
        tracing.interceptSuspendCall(fakeCallContext<Int>(name = "call", args = args2))
        val expected = listOf(
            fakeCallTrace(name = "call", args = args1),
            fakeCallTrace(name = "call", args = args2),
        )
        assertEquals(expected, tracing.all)
        assertEquals(expected, tracing.unverified)
    }

    @Test
    fun testResetClearsCalls()  {
        tracing.interceptCall(fakeCallContext<Int>(name = "call", args = args1))
        tracing.interceptCall(fakeCallContext<Int>(name = "call", args = args2))
        tracing.reset()
        assertEquals(emptyList(), tracing.all)
        assertEquals(emptyList(), tracing.unverified)
    }

    @Test
    fun testMarkVerifiedRemovesCallFromUnverified()  {
        tracing.interceptCall(fakeCallContext<Int>(name = "call", args = args1))
        tracing.interceptCall(fakeCallContext<Int>(name = "call", args = args2))
        val unverifiedWithoutFirst = tracing.unverified.drop(1)
        tracing.markVerified(tracing.unverified.first())
        assertEquals(unverifiedWithoutFirst, tracing.unverified)
    }

    @Test
    fun testMarkVerifiedDoesNotRemoveFromAll()  {
        tracing.interceptCall(fakeCallContext<Int>(name = "call", args = args1))
        tracing.interceptCall(fakeCallContext<Int>(name = "call", args = args2))
        val allSnapshot = tracing.all.toMutableList()
        tracing.markVerified(tracing.unverified.first())
        assertEquals(allSnapshot, tracing.all)
    }
}
