package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SoftVerifierTest {

    private val template1 = fakeCallTemplate(name = "call1")
    private val template2 = fakeCallTemplate(name = "call2")
    private val template3 = fakeCallTemplate(name = "call3")
    private val trace1 = fakeCallTrace(name = "call1")
    private val trace2 = fakeCallTrace(name = "call2")
    private val trace3 = fakeCallTrace(name = "call3")

    private val callMatcher = TestCallMatcher { trace, template ->
        when (trace) {
            trace1 if template == template1 -> CallMatchResult.Matching
            trace2 if template == template2 -> CallMatchResult.Matching
            trace3 if template == template3 -> CallMatchResult.Matching
            else -> CallMatchResult.NotMatching
        }
    }
    private val testRenderer = TestRenderer<SoftVerifier.Error> { "RENDERED_ERROR" }
    private val verifier = SoftVerifier(
        atLeast = 1,
        atMost = 2,
        callMatcher = callMatcher,
        errorRenderer = testRenderer
    )

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
            listOf(trace1, trace3, trace2, trace3),
            listOf(template1, template2)
        )
    }

    @Test
    fun testFailsWhenCallsDoesNotSatisfyAtLeast() {
        assertFailsWith<AssertionError> { verifier.verify(listOf(trace2, trace3), listOf(template1, template2)) }
        assertFailsWith<AssertionError> { verifier.verify(listOf(trace3, trace1), listOf(template1, template2)) }
        val expectedError1 = SoftVerifier.Error(
            expectedAtLeast = 1,
            expectedAtMost = 2,
            templateMatchingResults = TemplateGroupedMatchingResults(
                template = template1,
                calls = mapOf(CallMatchResult.NotMatching to listOf(trace2, trace3))
            )
        )
        val expectedError2 = SoftVerifier.Error(
            expectedAtLeast = 1,
            expectedAtMost = 2,
            templateMatchingResults = TemplateGroupedMatchingResults(
                template = template2,
                calls = mapOf(CallMatchResult.NotMatching to listOf(trace3, trace1))
            )
        )
        assertEquals(listOf(expectedError1, expectedError2), testRenderer.recordedCalls)
    }

    @Test
    fun testFailsWhenCallsDoesNotSatisfyAtMost() {
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace2, trace2, trace2, trace1), listOf(template1, template2))
        }
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace1, trace1, trace1, trace2), listOf(template1, template2))
        }
        val expectedError1 = SoftVerifier.Error(
            expectedAtLeast = 1,
            expectedAtMost = 2,
            templateMatchingResults = TemplateGroupedMatchingResults(
                template = template2,
                calls = mapOf(
                    CallMatchResult.Matching to listOf(trace2, trace2, trace2),
                    CallMatchResult.NotMatching to listOf(trace1)
                )
            )
        )
        val expectedError2 = SoftVerifier.Error(
            expectedAtLeast = 1,
            expectedAtMost = 2,
            templateMatchingResults = TemplateGroupedMatchingResults(
                template = template1,
                calls = mapOf(
                    CallMatchResult.Matching to listOf(trace1, trace1, trace1),
                    CallMatchResult.NotMatching to listOf(trace2)
                )
            )
        )
        assertEquals(listOf(expectedError1, expectedError2), testRenderer.recordedCalls)
    }

    @Test
    fun testReturnsAllTracesWhenMatchesStrictly() {
        val traces = listOf(trace1, trace2)
        val verified = verifier.verify(traces, listOf(template1, template2))
        assertEquals(traces, verified)
    }

    @Test
    fun testReturnsOnlyMatchingCallsWhenMatchesWithAdditionalCalls() {
        val traces = listOf(trace3, trace1, trace3, trace2, trace3)
        val verified = verifier.verify(traces, listOf(template1, template2))
        assertEquals(listOf(trace1, trace2), verified)
    }

    @Test
    fun testReturnsMultipleMatchingCallsWhenMoreThanOneMatch() {
        val traces = listOf(trace3, trace1, trace1, trace3, trace2, trace2, trace3)
        val verified = verifier.verify(traces, listOf(template1, template2))
        assertEquals(listOf(trace1, trace1, trace2, trace2), verified)
    }
}
