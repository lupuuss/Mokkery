package dev.mokkery.internal.verify

import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertFailsWith

class NotVerifierTest {


    private val template1 = fakeCallTemplate(name = "call1")
    private val template2 = fakeCallTemplate(name = "call2")
    private val trace1 = fakeCallTrace(name = "call1")
    private val trace2 = fakeCallTrace(name = "call2")


    private val callMatcher = TestCallMatcher { trace, template ->
        when {
            trace == trace1 && template == template1 -> true
            trace == trace2 && template == template2 -> true
            else -> false
        }
    }
    private val verifier = NotVerifier(callMatcher)

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
    fun testSuccessWhenNoMatchingCalls() {
        verifier.verify(listOf(fakeCallTrace("call3"), fakeCallTrace("call4")), listOf(template1, template2))
    }

    @Test
    fun testSuccessWhenNoCalls() {
        verifier.verify(listOf(), listOf(template1, template2))
    }
}
