package dev.mokkery.internal.matcher

import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.TestDefaultsMaterializer
import dev.mokkery.test.TestSignatureGenerator
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.fakeDefaultValueMatcher
import kotlin.test.Test
import kotlin.test.assertEquals

class CallMatcherTest {

    private val generator = TestSignatureGenerator()
    private val defaultsMaterializer = TestDefaultsMaterializer()
    private val matcher = CallMatcher(generator, defaultsMaterializer)

    init {
        generator.returns("call(i: kotlin.Int)")
    }

    @Test
    fun testReturnsMatchingForFullyMatchingCall() {
        val template = fakeCallTemplate(
            id = 1,
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        val call = fakeCallTrace(
            id = 1,
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertEquals(CallMatchResult.Matching, matcher.match(call, template))
    }


    @Test
    fun testReturnsMatchingForFullyMatchingCallWithDefaultsMaterialized() {
        val template = fakeCallTemplate(
            id = 1,
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf("i" to fakeDefaultValueMatcher())
        )
        val call = fakeCallTrace(
            id = 1,
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        defaultsMaterializer.calls = { _, it ->
            it.copy(matchers = it.matchers.plus("i" to MaterializedDefaultValueMatcher(1)))
        }
        assertEquals(CallMatchResult.Matching, matcher.match(call, template))
    }

    @Test
    fun testReturnsNotMatchingForNotMatchingMethodNames() {
        val template = fakeCallTemplate(
            id = 1,
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        generator.returns("calle(i: kotlin.Int)")
        val call = fakeCallTrace(
            id = 1,
            name = "calle",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertEquals(CallMatchResult.SameReceiver, matcher.match(call, template))
    }

    @Test
    fun testReturnsSameReceiverMethodOverloadForNotMatchingSignature() {
        val template = fakeCallTemplate(
            id = 1,
            name = "call",
            signature = "call(i: kotlin.Int, j: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1),
                "j" to ArgMatcher.Equals(1),
            )
        )
        val call = fakeCallTrace(
            id = 1,
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertEquals(CallMatchResult.SameReceiverMethodOverload, matcher.match(call, template))
    }

    @Test
    fun testReturnsSameReceiverMethodSignatureForNotMatchingArgsToSignature() {
        val template = fakeCallTemplate(
            id = 1,
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        val call = fakeCallTrace(
            id = 1,
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = ""))
        )
        assertEquals(CallMatchResult.SameReceiverMethodSignature, matcher.match(call, template))
    }

    @Test
    fun testReturnsNotMatchingForNotMatchingReceivers() {
        val template = fakeCallTemplate(
            id = 1,
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1)
            )
        )
        val call = fakeCallTrace(
            id = 2,
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertEquals(CallMatchResult.NotMatching, matcher.match(call, template))
    }

    @Test
    fun testReturnsSameReceiverMethodSignatureForNotSatisfiedMatcher() {
        val template = fakeCallTemplate(
            id = 1,
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(2)
            )
        )
        val call = fakeCallTrace(
            id = 1,
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertEquals(CallMatchResult.SameReceiverMethodSignature, matcher.match(call, template))
    }


    @Test
    fun testReturnsSameReceiverMethodSignatureForNotSatisfiedDefaultMatcher() {
        val template = fakeCallTemplate(
            id = 1,
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to fakeDefaultValueMatcher()
            )
        )
        val call = fakeCallTrace(
            id = 1,
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        defaultsMaterializer.calls = { _, it ->
            it.copy(matchers = it.matchers.plus("i" to MaterializedDefaultValueMatcher(2)))
        }
        assertEquals(CallMatchResult.SameReceiverMethodSignature, matcher.match(call, template))
    }

    @Test
    fun testReturnsSameReceiverMethodOverloadForNotMatchingArgNames() {
        val template = fakeCallTemplate(
            id = 1,
            name = "call",
            signature = "call(i: kotlin.Int)",
            matchers = mapOf(
                "i" to ArgMatcher.Equals(1),
                "j" to ArgMatcher.Equals(1),
            )
        )
        generator.returns("call(j: kotlin.Int)")
        val call = fakeCallTrace(
            id = 1,
            name = "call",
            args = listOf(fakeCallArg(name = "i", value = 1))
        )
        assertEquals(CallMatchResult.SameReceiverMethodOverload, matcher.match(call, template))
    }
}
