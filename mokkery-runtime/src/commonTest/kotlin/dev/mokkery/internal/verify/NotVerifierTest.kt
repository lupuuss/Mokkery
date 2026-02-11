package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.verify.Verifier.Result.Failure
import dev.mokkery.internal.verify.Verifier.Result.Success
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

class NotVerifierTest {

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
    private val verifier = NotVerifier(callMatcher = callMatcher)

    @Test
    fun testFailsWhenMultipleCallsMatch() {
        val actual = verifier.verify(listOf(trace1, trace1, trace3), listOf(template1, template2))
        val expected = Failure(NotVerifier.Error(template1, listOf(trace1, trace1)))
        assertEquals(expected, actual)
    }

    @Test
    fun testFailsWhenOneCallMatch() {
        val actual = verifier.verify(listOf(trace3, trace2), listOf(template1, template2))
        val expected = Failure(NotVerifier.Error(template2, listOf(trace2)))
        assertEquals(expected, actual)
    }

    @Test
    fun testSuccessWhenNoMatchingCalls() {
        assertEquals(
            expected = Success(emptyList()),
            actual = verifier.verify(listOf(trace3, trace3), listOf(template1, template2))
        )
    }

    @Test
    fun testSuccessWhenNoCalls() {
        assertEquals(
            expected = Success(emptyList()),
            actual = verifier.verify(listOf(), listOf(template1, template2))
        )
    }
}
