package dev.mokkery.internal.verify

import dev.mokkery.internal.calls.CallMatchResult
import dev.mokkery.test.StubRenderer
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
            trace == trace1 && template == template1 -> CallMatchResult.Matching
            trace == trace2 && template == template2 -> CallMatchResult.Matching
            else -> CallMatchResult.NotMatching
        }
    }
    private val verifier = SoftVerifier(
        atLeast = 1,
        atMost = 2,
        callMatcher = callMatcher,
        matchingResultsRenderer = StubRenderer
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

    @Test
    fun testFailsWithCorrectErrorMessageWhenSpecificRangeExpected() {
        val error = assertFailsWith<AssertionError> {
            SoftVerifier(3, 5, callMatcher, StubRenderer).verify(listOf(trace1), listOf(template1))
        }
        val expectedError = """
            Expected calls count to be in range 3..5, but 1 occurred for $template1!
            RENDERER_STUB
        """.trimIndent()
        assertEquals(expectedError, error.message)
    }

    @Test
    fun testFailsWithCorrectErrorMessageWhenAtLeastExpected() {
        val error = assertFailsWith<AssertionError> {
            SoftVerifier(2, Int.MAX_VALUE, callMatcher, StubRenderer).verify(listOf(trace1), listOf(template1))
        }
        val expectedError = """
            Expected at least 2 calls, but 1 occurred for $template1!
            RENDERER_STUB
        """.trimIndent()
        assertEquals(expectedError, error.message)
    }

    @Test
    fun testFailsWithCorrectErrorMessageWhenAtMostExpected() {
        val error = assertFailsWith<AssertionError> {
            SoftVerifier(1, 2, callMatcher, StubRenderer).verify(listOf(trace1, trace1, trace1), listOf(template1))
        }
        val expectedError = """
            Expected at most 2 calls, but 3 occurred for $template1!
            RENDERER_STUB
        """.trimIndent()
        assertEquals(expectedError, error.message)
    }


    @Test
    fun testFailsWithCorrectErrorMessageWhenExactNumberOfCallsExpected() {
        val error = assertFailsWith<AssertionError> {
            SoftVerifier(2, 2, callMatcher, StubRenderer).verify(listOf(trace1), listOf(template1))
        }
        val expectedError = """
            Expected exactly 2 calls, but 1 occurred for $template1!
            RENDERER_STUB
        """.trimIndent()
        assertEquals(expectedError, error.message)
    }

    @Test
    fun testFailsWithCorrectErrorMessageWhenAnyCallExpected() {
        val error = assertFailsWith<AssertionError> {
            SoftVerifier(1, Int.MAX_VALUE, callMatcher, StubRenderer).verify(listOf(), listOf(template1))
        }
        val expectedError = """
            Expected any call, but no matching calls for $template1!
            RENDERER_STUB
        """.trimIndent()
        assertEquals(expectedError, error.message)
    }
}
