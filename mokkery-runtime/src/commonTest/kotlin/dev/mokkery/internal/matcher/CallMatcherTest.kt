package dev.mokkery.internal.matcher

import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CallMatcherTest {

    private val matcher = CallMatcher()

    @Test
    fun testReturnsTrueForFullyMatchingCall() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        val call = fakeCallTrace(
            receiver = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertTrue(matcher.matches(call, template))
    }

    @Test
    fun testReturnsFalseForNotMatchingNames() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        val call = fakeCallTrace(
            receiver = "mock@1",
            name = "calle",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertFalse(matcher.matches(call, template))
    }

    @Test
    fun testReturnsFalseForNotMatchingSignature() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: Int, j: Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        val call = fakeCallTrace(
            receiver = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertFalse(matcher.matches(call, template))
    }

    @Test
    fun testReturnsFalseForNotMatchingArgsToSignature() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        val call = fakeCallTrace(
            receiver = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = ""))
        )
        assertFalse(matcher.matches(call, template))
    }

    @Test
    fun testReturnsFalseForNotMatchingReceivers() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        val call = fakeCallTrace(
            receiver = "mock@2",
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertFalse(matcher.matches(call, template))
    }

    @Test
    fun testReturnsFalseForNotSatisfiedMatcher() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(2)
            )
        )
        val call = fakeCallTrace(
            receiver = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertFalse(matcher.matches(call, template))
    }

    @Test
    fun testReturnsFalseForNotMatchingArgNames() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(2)
            )
        )
        val call = fakeCallTrace(
            receiver = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "j", value = 1))
        )
        assertFalse(matcher.matches(call, template))
    }
}
