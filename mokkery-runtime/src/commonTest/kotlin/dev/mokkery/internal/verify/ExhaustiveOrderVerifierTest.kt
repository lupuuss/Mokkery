package dev.mokkery.internal.verify

import dev.mokkery.test.TestCallMatcher
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
        when {
            trace == trace1 && template == template1 -> true
            trace == trace2 && template == template2 -> true
            else -> false
        }
    }
    private val verifier = ExhaustiveOrderVerifier(callMatcher)

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
