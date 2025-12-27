package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.verify.Verifier.Result.Failure
import dev.mokkery.internal.verify.Verifier.Result.Success
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.internal.verify.results.TemplateMatchingResultsComposer
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

class ExhaustiveOrderVerifierTest {

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

    private val resultsTestComposer = TemplateMatchingResultsComposer(callMatcher)
    private val verifier = ExhaustiveOrderVerifier(
        callMatcher = callMatcher,
        resultsComposer = resultsTestComposer,
    )

    @Test
    fun testFailsWhenNoCallsExpectedButOneHappened() {
        val actual = verifier.verify(listOf(trace1, trace2), listOf())
        val expected = Failure(
            error = ExhaustiveOrderVerifier.Error(
                results = listOf(
                    TemplateMatchingResult.UnverifiedCall(trace1),
                    TemplateMatchingResult.UnverifiedCall(trace2),
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testFailsWhenNoCandidatesForExpectedCall() {
        val actual = verifier.verify(listOf(trace1, trace2), listOf(template1, template2, template3))
        val expected = Failure(
            error = ExhaustiveOrderVerifier.Error(
                results = listOf(
                    TemplateMatchingResult.Matching(trace1, template1),
                    TemplateMatchingResult.Matching(trace2, template2),
                    TemplateMatchingResult.NoMatch(template3),
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testFailsWhenCallAtGivenPositionDoesNotMatch() {
        val actual = verifier.verify(listOf(trace1, trace2), listOf(template1, template1))
        val expected = Failure(
            error = ExhaustiveOrderVerifier.Error(
                results = listOf(
                    TemplateMatchingResult.Matching(trace1, template1),
                    TemplateMatchingResult.NoMatch(template1),
                    TemplateMatchingResult.UnverifiedCall(trace2),
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testFailsWhenUnverifiedCallsBeforeAndAfter() {
        val actual = verifier.verify(listOf(trace1, trace2, trace3), listOf(template2))
        val expected = Failure(
            error = ExhaustiveOrderVerifier.Error(
                results = listOf(
                    TemplateMatchingResult.UnverifiedCall(trace1),
                    TemplateMatchingResult.Matching(trace2, template2),
                    TemplateMatchingResult.UnverifiedCall(trace3),
                )
            )
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testSuccessWhenAllMatch() {
        val expected = Success(listOf(trace1, trace2))
        val actual = verifier.verify(listOf(trace1, trace2), listOf(template1, template2))
        assertEquals(expected, actual)
    }
}
