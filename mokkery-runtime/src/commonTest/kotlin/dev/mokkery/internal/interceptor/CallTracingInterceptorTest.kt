package dev.mokkery.internal.interceptor

import dev.mokkery.internal.Counter
import dev.mokkery.internal.context.MokkeryTools
import dev.mokkery.test.TestNextCallInterceptor
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.runTest
import dev.mokkery.test.testBlockingCallScope
import dev.mokkery.test.testSuspendCallScope
import kotlin.test.Test
import kotlin.test.assertEquals

class CallTracingInterceptorTest {

    private val nextInterceptor = TestNextCallInterceptor()
    private var clockStamp = 0L
    private val counter = Counter { clockStamp }
    private val context = nextInterceptor + MokkeryTools(callsCounter = counter)
    private val tracing = CallTracingInterceptor()
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
        tracing.intercept(testBlockingCallScope<Int>(name = "call", args = args1, context = context))
        tracing.intercept(testBlockingCallScope<Int>(name = "call", args = args2, context = context))
        val expected = listOf(
            fakeCallTrace(name = "call", args = args1),
            fakeCallTrace(name = "call", args = args2),
        )
        assertEquals(expected, tracing.all)
        assertEquals(expected, tracing.unverified)
    }

    @Test
    fun testRegistersCallsOnInterceptSuspendCall() = runTest {
        tracing.intercept(testSuspendCallScope<Int>(name = "call", args = args1, context = context))
        tracing.intercept(testSuspendCallScope<Int>(name = "call", args = args2, context = context))
        val expected = listOf(
            fakeCallTrace(name = "call", args = args1),
            fakeCallTrace(name = "call", args = args2),
        )
        assertEquals(expected, tracing.all)
        assertEquals(expected, tracing.unverified)
    }

    @Test
    fun testResetClearsCalls()  {
        tracing.intercept(testBlockingCallScope<Int>(name = "call", args = args1, context = context))
        tracing.intercept(testBlockingCallScope<Int>(name = "call", args = args2, context = context))
        tracing.reset()
        assertEquals(emptyList(), tracing.all)
        assertEquals(emptyList(), tracing.unverified)
    }

    @Test
    fun testMarkVerifiedRemovesCallFromUnverified()  {
        tracing.intercept(testBlockingCallScope<Int>(name = "call", args = args1, context = context))
        tracing.intercept(testBlockingCallScope<Int>(name = "call", args = args2, context = context))
        val unverifiedWithoutFirst = tracing.unverified.drop(1)
        tracing.markVerified(tracing.unverified.first())
        assertEquals(unverifiedWithoutFirst, tracing.unverified)
    }

    @Test
    fun testMarkVerifiedDoesNotRemoveFromAll()  {
        tracing.intercept(testBlockingCallScope<Int>(name = "call", args = args1, context = context))
        tracing.intercept(testBlockingCallScope<Int>(name = "call", args = args2, context = context))
        val allSnapshot = tracing.all.toMutableList()
        tracing.markVerified(tracing.unverified.first())
        assertEquals(allSnapshot, tracing.all)
    }
}
