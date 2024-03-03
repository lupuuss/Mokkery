package dev.mokkery.internal.matcher

import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.TestSignatureGenerator
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import kotlin.test.Test
import kotlin.test.assertEquals

class CallMatcherTest {

    private val generator = TestSignatureGenerator()
    private val matcher = CallMatcher(generator)

    init {
        generator.returns("call(i: kotlin.Int)")
    }

    @Test
    fun testReturnsMatchingForFullyMatchingCall() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        val call = fakeCallTrace(
            receiver = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertEquals(CallMatchResult.Matching, matcher.match(call, template))
    }

    @Test
    fun testReturnsNotMatchingForNotMatchingMethodNames() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        generator.returns("calle(i: kotlin.Int)")
        val call = fakeCallTrace(
            receiver = "mock@1",
            name = "calle",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertEquals(CallMatchResult.SameReceiver, matcher.match(call, template))
    }

    @Test
    fun testReturnsSameReceiverMethodOverloadForNotMatchingSignature() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: kotlin.Int, j: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1),
                "j" to ArgMatcher.Equals(1),
            )
        )
        val call = fakeCallTrace(
            receiver = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertEquals(CallMatchResult.SameReceiverMethodOverload, matcher.match(call, template))
    }

    @Test
    fun testReturnsSameReceiverMethodSignatureForNotMatchingArgsToSignature() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        val call = fakeCallTrace(
            receiver = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = ""))
        )
        assertEquals(CallMatchResult.SameReceiverMethodSignature, matcher.match(call, template))
    }

    @Test
    fun testReturnsNotMatchingForNotMatchingReceivers() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        val call = fakeCallTrace(
            receiver = "mock@2",
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertEquals(CallMatchResult.NotMatching, matcher.match(call, template))
    }

    @Test
    fun testReturnsSameReceiverMethodSignatureForNotSatisfiedMatcher() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(2)
            )
        )
        val call = fakeCallTrace(
            receiver = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertEquals(CallMatchResult.SameReceiverMethodSignature, matcher.match(call, template))
    }

    @Test
    fun testReturnsSameReceiverMethodOverloadForNotMatchingArgNames() {
        val template = fakeCallTemplate(
            receiver = "mock@1",
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1),
                "j" to ArgMatcher.Equals(1),
            )
        )
        generator.returns("call(j: kotlin.Int)")
        val call = fakeCallTrace(
            receiver = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertEquals(CallMatchResult.SameReceiverMethodOverload, matcher.match(call, template))
    }
}
