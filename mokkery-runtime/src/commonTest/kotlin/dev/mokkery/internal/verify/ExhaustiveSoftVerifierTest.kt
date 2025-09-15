package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.test.StubRenderer
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
        when (trace) {
            trace1 if template == template1 -> CallMatchResult.Matching
            trace2 if template == template2 -> CallMatchResult.Matching
            else -> CallMatchResult.NotMatching
        }
    }
    private val verifier = ExhaustiveSoftVerifier(
        callMatcher = callMatcher,
        matchingResultsRenderer = StubRenderer("RESULTS"),
        unverifiedCallsRenderer = StubRenderer("UNVERIFIED")
    )

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
        val verified = verifier.verify(traces, listOf(template1, template2))
        assertEquals(traces, verified)
    }

    @Test
    fun testFailsWithCorrectMessageWhenMissingResults() {
        val error = assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace1), listOf(template1, template2))
        }
        val expectedError = """
            No matching call for mock(1).call2()!
            RENDERER_RESULTS
        """.trimIndent()
        assertEquals(expectedError, error.message)
    }

    @Test
    fun testFailsWithCorrectMessageWhenUnverifiedCalls() {
        val error = assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace1, trace2), listOf(template1))
        }
        assertEquals("RENDERER_UNVERIFIED", error.message)
    }
}
