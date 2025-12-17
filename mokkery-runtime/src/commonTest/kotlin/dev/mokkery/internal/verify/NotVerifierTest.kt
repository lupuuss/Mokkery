package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.test.StubRenderer
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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
    private val testRenderer = TestRenderer<NotVerifier.Error> { "RENDERED_ERROR" }
    private val verifier = NotVerifier(callMatcher = callMatcher, errorRenderer = testRenderer)

    @Test
    fun testFailsWhenMultipleCallsMatch() {
        val error = assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace1, trace1, trace3), listOf(template1, template2))
        }
        assertEquals(NotVerifier.Error(template1, listOf(trace1, trace1)), testRenderer.recordedCalls.single())
        assertEquals("RENDERED_ERROR", error.message)
    }

    @Test
    fun testFailsWhenOneCallMatch() {
        val error = assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace3, trace2), listOf(template1, template2))
        }
        assertEquals(NotVerifier.Error(template2, listOf(trace2)), testRenderer.recordedCalls.single())
        assertEquals("RENDERED_ERROR", error.message)
    }

    @Test
    fun testSuccessWhenNoMatchingCalls() {
        verifier.verify(listOf(trace3, trace3), listOf(template1, template2))
    }

    @Test
    fun testSuccessWhenNoCalls() {
        verifier.verify(listOf(), listOf(template1, template2))
    }
}
