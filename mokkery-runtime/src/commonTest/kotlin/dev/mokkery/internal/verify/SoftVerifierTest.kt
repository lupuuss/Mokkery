package dev.mokkery.internal.verify

import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SoftVerifierTest {

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
    private val verifier = SoftVerifier(1, 2, callMatcher)

    @Test
    fun testSuccessWhenCallsStrictlySatisfiesAtLeast() {
        verifier.verify(listOf(trace1, trace2), listOf(template1, template2))
    }

    @Test
    fun testSuccessWhenCallsStrictlySatisfiesAtMost() {
        verifier.verify(listOf(trace1, trace2, trace1, trace2), listOf(template1, template2))
    }

    @Test
    fun testSuccessWhenCallsStrictlySatisfiesWithOtherCalls() {
        verifier.verify(
            listOf(trace1, fakeCallTrace(name = "call3"), trace2, fakeCallTrace(name = "call3")),
            listOf(template1, template2)
        )
    }

    @Test
    fun testFailsWhenCallsDoesNotSatisfyAtLeast() {
        assertFailsWith<AssertionError> {
            verifier.verify(
                listOf(trace2, fakeCallTrace(name = "call3")),
                listOf(template1, template2)
            )
        }
        assertFailsWith<AssertionError> {
            verifier.verify(
                listOf(fakeCallTrace(name = "call3"), trace1),
                listOf(template1, template2)
            )
        }
    }

    @Test
    fun testFailsWhenCallsDoesNotSatisfyAtMost() {
        assertFailsWith<AssertionError> {
            verifier.verify(
                listOf(trace2, trace2, trace2, trace1),
                listOf(template1, template2)
            )
        }
        assertFailsWith<AssertionError> {
            verifier.verify(
                listOf(trace1, trace1, trace1, trace2),
                listOf(template1, template2)
            )
        }
    }

    @Test
    fun testReturnsAllTracesWhenMatchesStrictly() {
        val traces = listOf(trace1, trace2)
        val verified = verifier.verify(traces, listOf(template1, template2))
        assertEquals(traces, verified)
    }

    @Test
    fun testReturnsOnlyMatchingCallsWhenMatchesWithAdditionalCalls() {
        val traces = listOf(
            fakeCallTrace(name = "call3"),
            trace1,
            fakeCallTrace(name = "call3"),
            trace2,
            fakeCallTrace(name = "call3"),
        )
        val verified = verifier.verify(traces, listOf(template1, template2))
        assertEquals(listOf(trace1, trace2), verified)
    }

    @Test
    fun testReturnsMultipleMatchingCallsWhenMoreThanOneMatch() {
        val traces = listOf(
            fakeCallTrace(name = "call3"),
            trace1,
            trace1,
            fakeCallTrace(name = "call3"),
            trace2,
            trace2,
            fakeCallTrace(name = "call3"),
        )
        val verified = verifier.verify(traces, listOf(template1, template2))
        assertEquals(listOf(trace1, trace1, trace2, trace2), verified)
    }
}
