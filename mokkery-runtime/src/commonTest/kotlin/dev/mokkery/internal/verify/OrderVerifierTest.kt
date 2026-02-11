package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.verify.Verifier.Result.Success
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

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
    private val verifier = OrderVerifier(
        callMatcher = callMatcher,
        resultsComposer = resultsTestComposer
    )

    @Test
    fun testSuccessWhenOrderIsStrictlySatisfied() {
        assertEquals(
            expected = Success(listOf(trace1, trace2)),
            actual = verifier.verify(listOf(trace1, trace2), listOf(template1, template2))
        )
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
        assertEquals(
            expected = Success(listOf(trace1, trace2)),
            actual = verifier.verify(traces, listOf(template1, template2))
        )
    }

    @Test
    fun testReturnsAllTracesWhenStrictlySatisfied() {
        val traces = listOf(trace1, trace2)
        assertEquals(
            expected = Success(traces),
            actual = verifier.verify(traces, listOf(template1, template2))
        )
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
        assertEquals(
            expected = Success(listOf(trace1, trace2)),
            actual = verifier.verify(traces, listOf(template1, template2))
        )
    }

    @Test
    fun testFailsWhenOrderIsNotSatisfiedWithoutAdditionalCalls() {
        val traces = listOf(trace2, trace1)
        val templates = listOf(template1, template2)
        val actual = verifier.verify(traces, templates)
        val expected = Verifier.Result.Failure(
            error = OrderVerifier.Error(
                failedAt = template2,
                failedIndex = 1,
                results = listOf(
                    TemplateMatchingResult.UnverifiedCall(trace2),
                    TemplateMatchingResult.Matching(trace1, template1),
                    TemplateMatchingResult.NoMatch(template2)
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testFailsWhenOrderIsNotSatisfiedWithAdditionalCalls() {
        val trace0 = fakeCallTrace(name = "call0")
        val traces = listOf(trace0, trace2, trace0, trace1, trace0)
        val templates = listOf(template1, template2)
        val actual = verifier.verify(traces, templates)
        val expected = Verifier.Result.Failure(
            error = OrderVerifier.Error(
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
            )
        )
        assertEquals(expected, actual)
    }
}
