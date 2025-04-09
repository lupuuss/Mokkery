package dev.mokkery.internal.verify

import dev.mokkery.internal.calls.CallMatchResult
import dev.mokkery.internal.verify.results.TemplateMatchingResult
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.TestRenderer
import dev.mokkery.test.TestTemplateMatchingResultComposer
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
        when {
            trace == trace1 && template == template1 -> CallMatchResult.Matching
            trace == trace2 && template == template2 -> CallMatchResult.Matching
            else -> CallMatchResult.NotMatching
        }
    }
    private val resultsTestComposer = TestTemplateMatchingResultComposer()
    private val testRenderer = TestRenderer<List<TemplateMatchingResult>> { "RENDERER_RESULTS" }
    private val verifier = OrderVerifier(callMatcher, resultsTestComposer, testRenderer)

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
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace2, trace1), listOf(template1, template2))
        }
    }

    @Test
    fun testFailsWhenOrderIsNotSatisfiedWithAdditionalCalls() {
        assertFailsWith<AssertionError> {
            val traces = listOf(
                fakeCallTrace(name = "call0"),
                trace2,
                fakeCallTrace(name = "call0"),
                trace1,
                fakeCallTrace(name = "call0")
            )
            verifier.verify(traces, listOf(template1, template2))
        }
    }

    @Test
    fun testFailsWithCorrectMessage() {
        val error = assertFailsWith<AssertionError> {
            val traces = listOf(trace2, trace1, trace2)
            val templates = listOf(template1, template2, template1)
            verifier.verify(traces, templates)
        }
        val expectedMessage = """
            Expected calls in specified order but not satisfied! Failed at 3. mock(1).call1()!
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
}
