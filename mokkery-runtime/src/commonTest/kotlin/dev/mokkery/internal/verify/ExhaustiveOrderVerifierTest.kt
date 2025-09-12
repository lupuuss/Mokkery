package dev.mokkery.internal.verify

import dev.mokkery.internal.matcher.CallMatchResult
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.TestTemplateMatchingResultComposer
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExhaustiveOrderVerifierTest {
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

    private val resultsTestComposer = TestTemplateMatchingResultComposer()
    private val testRenderer = TestRenderer<List<TemplateMatchingResult>> { "RENDERER_RESULTS" }
    private val verifier = ExhaustiveOrderVerifier(callMatcher, resultsTestComposer, testRenderer)

    @Test
    fun testFailsWhenNumberOfTemplatesAndTracesDiffers() {
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(fakeCallTrace()), listOf())
        }
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(fakeCallTrace()), listOf(fakeCallTemplate(), fakeCallTemplate()))
        }
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(fakeCallTrace(), fakeCallTrace()), listOf(fakeCallTemplate()))
        }
    }

    @Test
    fun testFailsWhenMatcherReturnsFalseForAtLeastOneCall() {
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace1, trace2), listOf(template1, fakeCallTemplate()))
        }
    }

    @Test
    fun testFailsWithCorrectMessage() {
        val error = assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace1, trace2), listOf(template1, fakeCallTemplate()))
        }
        val expectedMessage = """
            Expected strict order of calls without unverified ones, but not satisfied!
            RENDERER_RESULTS
        """.trimIndent()
        assertEquals(expectedMessage, error.message)
    }

    @Test
    fun testUsesComposerResultsToRenderError() {
        val traces = listOf(trace2, trace1, trace2)
        val templates = listOf(template1, template2, template1)
        val results = listOf(TemplateMatchingResult.UnverifiedCall(trace1))
        resultsTestComposer.returns(results)
        assertFailsWith<AssertionError> { verifier.verify(traces, templates) }
        assertEquals(traces to templates, resultsTestComposer.recordedCalls.single())
        assertEquals(results, testRenderer.recordedCalls.single())
    }

    @Test
    fun testSuccessWhenMatcherReturnsTrue() {
        callMatcher.returnsMany(true, true)
        verifier.verify(listOf(trace1, trace2), listOf(template1, template2))
    }

    @Test
    fun testReturnsAllTracesOnSuccess() {
        callMatcher.returnsMany(true, true)
        val verified = verifier.verify(listOf(trace1, trace2), listOf(template1, template2))
        assertEquals(listOf(trace1, trace2), verified)
    }
}
