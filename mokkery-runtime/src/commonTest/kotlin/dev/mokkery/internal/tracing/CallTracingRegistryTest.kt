package dev.mokkery.internal.tracing

import dev.mokkery.internal.Counter
import dev.mokkery.internal.context.MokkeryTools
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.testBlockingCallScope
import kotlin.test.Test
import kotlin.test.assertEquals

class CallTracingRegistryTest {

    private var clockStamp = 0L
    private val counter = Counter { clockStamp }
    private val context = MokkeryTools(callsCounter = counter)
    private val tracing = CallTracingRegistry()
    private val args1 = listOf(
        fakeCallArg(name = "i", value = 1),
        fakeCallArg(name = "j", value = 1),
    )
    private val args2 = listOf(
        fakeCallArg(name = "firstName", value = "John"),
        fakeCallArg(name = "lastName", value = "Doe"),
    )

    @Test
    fun testTraceSavesCallsProperly() {
        tracing.trace(testBlockingCallScope<Int>(name = "call", args = args1, context = context))
        tracing.trace(testBlockingCallScope<Int>(name = "call", args = args2, context = context))
        val expected = listOf(fakeCallTrace(name = "call", args = args1), fakeCallTrace(name = "call", args = args2))
        assertEquals(expected, tracing.all)
        assertEquals(expected, tracing.unverified)
    }

    @Test
    fun testResetClearsCalls()  {
        tracing.trace(testBlockingCallScope<Int>(name = "call", args = args1, context = context))
        tracing.trace(testBlockingCallScope<Int>(name = "call", args = args2, context = context))
        tracing.reset()
        assertEquals(emptyList(), tracing.all)
        assertEquals(emptyList(), tracing.unverified)
    }

    @Test
    fun testMarkVerifiedRemovesCallFromUnverified()  {
        tracing.trace(testBlockingCallScope<Int>(name = "call", args = args1, context = context))
        tracing.trace(testBlockingCallScope<Int>(name = "call", args = args2, context = context))
        val unverifiedWithoutFirst = tracing.unverified.drop(1)
        tracing.markVerified(tracing.unverified.first())
        assertEquals(unverifiedWithoutFirst, tracing.unverified)
    }

    @Test
    fun testMarkVerifiedDoesNotRemoveFromAll()  {
        tracing.trace(testBlockingCallScope<Int>(name = "call", args = args1, context = context))
        tracing.trace(testBlockingCallScope<Int>(name = "call", args = args2, context = context))
        val allSnapshot = tracing.all.toMutableList()
        tracing.markVerified(tracing.unverified.first())
        assertEquals(allSnapshot, tracing.all)
    }
}
