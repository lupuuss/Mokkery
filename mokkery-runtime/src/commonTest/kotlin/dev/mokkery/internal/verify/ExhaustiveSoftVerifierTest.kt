package dev.mokkery.internal.verify

import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExhaustiveSoftVerifierTest {

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
    private val verifier = ExhaustiveSoftVerifier(callMatcher)

    @Test
    fun testSuccessWhenAllCallsMatch() {
        verifier.verify(listOf(trace1, trace2), listOf(template1, template2))
    }

    @Test
    fun testSuccessWhenAllCallsInChangedOrder() {
        verifier.verify(listOf(trace2, trace1), listOf(template1, template2))
    }

    @Test
    fun testSuccessWhenAllCallsMatchWithMultipleMatchesPerTemplate() {
        verifier.verify(listOf(trace2, trace2, trace1, trace1), listOf(template1, template2))
    }

    @Test
    fun testFailsWhenAdditionalCalls() {
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace1, trace2, fakeCallTrace(name = "call3")), listOf(template1, template2))
        }
    }

    @Test
    fun testFailsWhenAdditionalCallsInChangedOrder() {
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace2, trace1, fakeCallTrace(name = "call3")), listOf(template1, template2))
        }
    }

    @Test
    fun testFailsWhenMissingCallForTemplate() {
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace2, trace1), listOf(template1, template2, fakeCallTemplate(name = "call3")))
        }
    }

    @Test
    fun testReturnsAllTracesOnSuccess() {
        val traces = listOf(trace2, trace1)
        val verified = verifier.verify(traces, listOf(template1, template2,))
        assertEquals(traces, verified)
    }
}
