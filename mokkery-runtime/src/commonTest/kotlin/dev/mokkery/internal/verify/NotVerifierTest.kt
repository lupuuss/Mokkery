package dev.mokkery.internal.verify

import dev.mokkery.internal.calls.CallMatchResult
import dev.mokkery.test.StubRenderer
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NotVerifierTest {


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
    private val verifier = NotVerifier(callMatcher, StubRenderer())

    @Test
    fun testFailsWhenAnyCallsMatches() {
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace1, fakeCallTrace("call3")), listOf(template1, template2))
        }
        assertFailsWith<AssertionError> {
            verifier.verify(listOf(fakeCallTrace("call3"), trace2), listOf(template1, template2))
        }
    }

    @Test
    fun testFailsWithCorrectMessage() {
        val error = assertFailsWith<AssertionError> {
            verifier.verify(listOf(trace1), listOf(template1))
        }
        val expectedMessage = """
            Calls to mock(1).call1() were not expected, but occurred:
            RENDERER_STUB
        """.trimIndent()
        assertEquals(expectedMessage, error.message)
    }

    @Test
    fun testSuccessWhenNoMatchingCalls() {
        verifier.verify(listOf(fakeCallTrace("call3"), fakeCallTrace("call4")), listOf(template1, template2))
    }

    @Test
    fun testSuccessWhenNoCalls() {
        verifier.verify(listOf(), listOf(template1, template2))
    }
}
