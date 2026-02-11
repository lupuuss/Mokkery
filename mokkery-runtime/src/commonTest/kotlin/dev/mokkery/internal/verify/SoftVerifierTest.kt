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
    private val verifier = SoftVerifier(
        atLeast = 1,
        atMost = 2,
        callMatcher = callMatcher,
    )

    @Test
    fun testSuccessWhenCallsStrictlySatisfiesAtLeast() {
        assertEquals(
            expected = Success(listOf(trace1, trace2)),
            actual = verifier.verify(listOf(trace1, trace2), listOf(template1, template2))
        )
    }

    @Test
    fun testSuccessWhenCallsStrictlySatisfiesAtMost() {
        assertEquals(
            expected = Success(listOf(trace1, trace1, trace2, trace2)),
            actual = verifier.verify(listOf(trace1, trace2, trace1, trace2), listOf(template1, template2))
        )
    }

    @Test
    fun testSuccessWhenCallsStrictlySatisfiesWithOtherCalls() {
        assertEquals(
            expected = Success(listOf(trace1, trace2)),
            actual = verifier.verify(
                listOf(trace1, trace3, trace2, trace3),
                listOf(template1, template2)
            )
        )
    }

    @Test
    fun testFailsWhenCallsDoesNotSatisfyAtLeast() {
        val actual1 = verifier.verify(listOf(trace2, trace3), listOf(template1, template2))
        val actual2 = verifier.verify(listOf(trace3, trace1), listOf(template1, template2))
        val expected1 = Failure(
            SoftVerifier.Error(
                expectedAtLeast = 1,
                expectedAtMost = 2,
                templateMatchingResults = TemplateGroupedMatchingResults(
                    template = template1,
                    calls = mapOf(CallMatchResult.NotMatching to listOf(trace2, trace3))
                )
            )
        )
        val expected2 = Failure(
            SoftVerifier.Error(
                expectedAtLeast = 1,
                expectedAtMost = 2,
                templateMatchingResults = TemplateGroupedMatchingResults(
                    template = template2,
                    calls = mapOf(CallMatchResult.NotMatching to listOf(trace3, trace1))
                )
            )
        )
        assertEquals(expected1, actual1)
        assertEquals(expected2, actual2)
    }

    @Test
    fun testFailsWhenCallsDoesNotSatisfyAtMost() {
        val actual1 = verifier.verify(listOf(trace2, trace2, trace2, trace1), listOf(template1, template2))
        val actual2 = verifier.verify(listOf(trace1, trace1, trace1, trace2), listOf(template1, template2))
        val expected1 = Failure(
            SoftVerifier.Error(
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
        )
        val expected2 = Failure(
            SoftVerifier.Error(
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
        )
        assertEquals(expected1, actual1)
        assertEquals(expected2, actual2)
    }
}
