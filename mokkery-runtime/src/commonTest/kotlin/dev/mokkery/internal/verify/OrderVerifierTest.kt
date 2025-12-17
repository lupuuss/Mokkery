package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.TestRenderer
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
        when (trace) {
            trace1 if template == template1 -> CallMatchResult.Matching
            trace2 if template == template2 -> CallMatchResult.Matching
            else -> CallMatchResult.NotMatching
        }
    }
    private val resultsTestComposer = TemplateMatchingResultsComposer(callMatcher)
    private val testRenderer = TestRenderer<OrderVerifier.Error> { "RENDERED_ERROR" }
    private val verifier = OrderVerifier(
        callMatcher = callMatcher,
        resultsComposer = resultsTestComposer,
        errorRenderer = testRenderer
    )

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

    @Test
    fun testFailsWhenOrderIsNotSatisfiedWithoutAdditionalCalls() {
        val traces = listOf(trace2, trace1)
        val templates = listOf(template1, template2)
        val error = assertFailsWith<AssertionError> { verifier.verify(traces, templates) }
        assertEquals("RENDERED_ERROR", error.message)
        assertEquals(
            OrderVerifier.Error(
                failedAt = template2,
                failedIndex = 1,
                results = listOf(
                    TemplateMatchingResult.UnverifiedCall(trace2),
                    TemplateMatchingResult.Matching(trace1, template1),
                    TemplateMatchingResult.NoMatch(template2)
                )
            ),
            testRenderer.recordedCalls.single()
        )
    }

    @Test
    fun testFailsWhenOrderIsNotSatisfiedWithAdditionalCalls() {
        val trace0 = fakeCallTrace(name = "call0")
        val traces = listOf(trace0, trace2, trace0, trace1, trace0)
        val templates = listOf(template1, template2)
        val error = assertFailsWith<AssertionError> { verifier.verify(traces, templates) }
        assertEquals("RENDERED_ERROR", error.message)
        assertEquals(
            OrderVerifier.Error(
                failedAt = template2,
                failedIndex = 1,
                results = listOf(
                    TemplateMatchingResult.UnverifiedCall(trace0),
                    TemplateMatchingResult.UnverifiedCall(trace2),
                    TemplateMatchingResult.UnverifiedCall(trace0),
                    TemplateMatchingResult.Matching(trace1, template1),
                    TemplateMatchingResult.NoMatch(template2),
                    TemplateMatchingResult.UnverifiedCall(trace0),
                )
            ),
            testRenderer.recordedCalls.single()
        )
    }
}
