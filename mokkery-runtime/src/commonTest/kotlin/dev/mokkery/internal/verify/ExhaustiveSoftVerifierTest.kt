package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.verify.Verifier.Result.Failure
import dev.mokkery.internal.verify.Verifier.Result.Success
import dev.mokkery.internal.verify.results.TemplateGroupedMatchingResults
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

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
    private val verifier = ExhaustiveSoftVerifier(callMatcher = callMatcher)

    @Test
    fun testSuccessWhenAllCallsMatch() {
        assertEquals(
            expected = Success(listOf(trace1, trace2)),
            actual = verifier.verify(listOf(trace1, trace2), listOf(template1, template2)),
        )
    }

    @Test
    fun testSuccessWhenAllCallsInChangedOrder() {
        assertEquals(
            expected = Success(listOf(trace2, trace1)),
            actual = verifier.verify(listOf(trace2, trace1), listOf(template1, template2)),
        )
    }

    @Test
    fun testSuccessWhenAllCallsMatchWithMultipleMatchesPerTemplate() {
        assertEquals(
            expected = Success(listOf(trace2, trace2, trace1, trace1)),
            actual = verifier.verify(listOf(trace2, trace2, trace1, trace1), listOf(template1, template2)),
        )
    }

    @Test
    fun testFailsWhenAdditionalCalls() {
        val trace3 = fakeCallTrace(name = "call3")
        val traces = listOf(trace1, trace2, trace3)
        val templates = listOf(template1, template2)
        val actual = verifier.verify(traces, templates)
        val expected = Failure(ExhaustiveSoftVerifier.Error.UnverifiedCalls(listOf(trace3)))
        assertEquals(expected, actual)
    }

    @Test
    fun testFailsWhenAdditionalCallsInChangedOrder() {
        val trace3 = fakeCallTrace(name = "call3")
        val traces = listOf(trace2, trace1, trace3)
        val templates = listOf(template1, template2)
        val actual = verifier.verify(traces, templates)
        val expected = Failure(ExhaustiveSoftVerifier.Error.UnverifiedCalls(listOf(trace3)))
        assertEquals(expected, actual)
    }

    @Test
    fun testFailsWhenMissingCallForTemplate() {
        val template3 = fakeCallTemplate(name = "call3")
        val traces = listOf(trace2, trace1)
        val templates = listOf(template1, template2, template3)
        val actual = verifier.verify(traces, templates)
        val expected = Failure(
            error = ExhaustiveSoftVerifier.Error.NoMatch(
                templateMatchingResults = TemplateGroupedMatchingResults(
                    template = template3,
                    calls = mapOf(CallMatchResult.NotMatching to listOf(trace2, trace1))
                )
            )
        )
        assertEquals(expected, actual)
    }
}
