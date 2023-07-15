package dev.mokkery.internal.verify

import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class OrderVerifierTest {

    private val template1 = fakeCallTemplate(name = "call1")
    private val template2 = fakeCallTemplate(name = "call2")
    private val trace1 = fakeCallTrace(name = "call1")
    private val trace2 = fakeCallTrace(name = "call2")


    private val callMatcher = TestCallMatcher { trace, template ->
        when {
            trace == trace1 && template == template1 -> true
            trace == trace2 && template == template2 -> true
            else -> false
        }
    }
    private val verifier = OrderVerifier(callMatcher)

    @Test
    fun testSuccessWhenOrderIsStrictlySatisfied() {
        verifier.verify(listOf(trace1, trace2), listOf(template1, template2))
    }

    @Test
    fun testSuccessWhenOrderIsSatisfiedWithCallsInBetween() {
        val traces = listOf(
            fakeCallTrace(name = "call0"),
            trace1,
            fakeCallTrace(name = "call0"),
            trace2,
            fakeCallTrace(name = "call0")
        )
        verifier.verify(traces, listOf(template1, template2))
    }

    @Test
    fun testFailsWhenOrderIsNotSatisfiedWithoutAdditionalCalls() {
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace2, trace1), listOf(template1, template2))
        }
    }

    @Test
    fun testFailsWhenOrderIsNotSatisfiedWithAdditionalCalls() {
        assertFailsWith<AssertionError> {
            val traces = listOf(
                fakeCallTrace(name = "call0"),
                trace2,
                fakeCallTrace(name = "call0"),
                trace1,
                fakeCallTrace(name = "call0")
            )
            verifier.verify(traces, listOf(template1, template2))
        }
    }

    @Test
    fun testReturnsAllTracesWhenStrictlySatisfied() {
        val traces = listOf(trace1, trace2)
        val verified = verifier.verify(traces, listOf(template1, template2))
        assertEquals(traces, verified)
    }

    @Test
    fun testReturnsOnlyExpectedTracesWhenSatisfiedWithAdditionalCalls() {
        val traces = listOf(
            fakeCallTrace(name = "call0"),
            trace1,
            fakeCallTrace(name = "call0"),
            trace2,
            fakeCallTrace(name = "call0")
        )
        val verified = verifier.verify(traces, listOf(template1, template2))
        assertEquals(listOf(trace1, trace2), verified)
    }
}
