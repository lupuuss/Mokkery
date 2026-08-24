package dev.mokkery.internal.matcher

import dev.mokkery.internal.MokkeryCollection
import dev.mokkery.internal.context.functions
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.TestDefaultsMaterializer
import dev.mokkery.test.TestMemberFunctions
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallEntry
import dev.mokkery.test.fakeDefaultValueMatcher
import dev.mokkery.test.fakeFunParam
import dev.mokkery.test.fakeFunction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CallMatcherTest {

    private val scope = TestMokkeryInstanceScope(
        functions = listOf(
            fakeFunction(name = "call", parameters = listOf(fakeFunParam<Int>("i"))),
            fakeFunction(name = "calle", parameters = listOf(fakeFunParam<Int>("i"))),
            fakeFunction(
                name = "call",
                id = CALL_OVERLOAD_ID,
                parameters = listOf(fakeFunParam<Int>("i"), fakeFunParam<Int>("j")),
            ),
        )
    )
    private val defaultsMaterializer = TestDefaultsMaterializer()
    private val matcher = CallMatcher(MokkeryCollection(listOf(scope)), defaultsMaterializer)
    private val memberFunctions get() = scope.functions as TestMemberFunctions

    @Test
    fun testReturnsMatchingForFullyMatchingCall() {
        val template = fakeCallTemplate(ArgMatcher.Equals(1), name = "call")
        val entry = fakeCallEntry(name = "call", args = listOf(1))
        assertEquals(CallMatchResult.Matching, matcher.match(template, entry))
    }

    @Test
    fun testReturnsMatchingForFullyMatchingCallWithDefaultsMaterialized() {
        val template = fakeCallTemplate(fakeDefaultValueMatcher(), name = "call")
        val entry = fakeCallEntry(name = "call", args = listOf(1))
        defaultsMaterializer.calls = { it, _ ->
            it.copy(matchers = listOf(MaterializedDefaultValueMatcher(1)))
        }
        assertEquals(CallMatchResult.Matching, matcher.match(template, entry))
    }

    @Test
    fun testReturnsSameReceiverForNotMatchingMethodNames() {
        val template = fakeCallTemplate(ArgMatcher.Equals(1), name = "call")
        val entry = fakeCallEntry(name = "calle", args = listOf(1))
        assertEquals(CallMatchResult.SameReceiver, matcher.match(template, entry))
    }

    @Test
    fun testReturnsSameReceiverMethodOverloadForNotMatchingSignature() {
        val template = fakeCallTemplate(ArgMatcher.Equals(1), ArgMatcher.Equals(1), functionId = CALL_OVERLOAD_ID)
        val entry = fakeCallEntry(name = "call", args = listOf(1))
        assertEquals(CallMatchResult.SameReceiverMethodOverload, matcher.match(template, entry))
    }

    @Test
    fun testReturnsSameReceiverMethodSignatureForNotMatchingArgsToSignature() {
        val template = fakeCallTemplate(ArgMatcher.Equals(1), name = "call")
        val entry = fakeCallEntry(name = "call", args = listOf(2))
        assertEquals(CallMatchResult.SameReceiverMethodSignature, matcher.match(template, entry))
    }

    @Test
    fun testReturnsNotMatchingForNotMatchingReceivers() {
        val template = fakeCallTemplate(ArgMatcher.Equals(1), instanceId = 1, name = "call")
        val entry = fakeCallEntry(id = 2, name = "call", args = listOf(1))
        assertEquals(CallMatchResult.NotMatching, matcher.match(template, entry))
    }

    @Test
    fun testReturnsSameReceiverMethodSignatureForNotSatisfiedMatcher() {
        val template = fakeCallTemplate(ArgMatcher.Equals(2), name = "call")
        val entry = fakeCallEntry(name = "call", args = listOf(1))
        assertEquals(CallMatchResult.SameReceiverMethodSignature, matcher.match(template, entry))
    }

    @Test
    fun testAreMatchingReturnsTrueForFullyMatchingCall() {
        val template = fakeCallTemplate(ArgMatcher.Equals(1), name = "call")
        val entry = fakeCallEntry(name = "call", args = listOf(1))
        assertTrue(matcher.areMatching(template, entry))
    }

    @Test
    fun testAreMatchingReturnsTrueForFullyMatchingCallWithDefaultsMaterialized() {
        val template = fakeCallTemplate(fakeDefaultValueMatcher(), name = "call")
        val entry = fakeCallEntry(name = "call", args = listOf(1))
        defaultsMaterializer.calls = { it, _ ->
            it.copy(matchers = listOf(MaterializedDefaultValueMatcher(1)))
        }
        assertTrue(matcher.areMatching(template, entry))
    }

    @Test
    fun testAreMatchingReturnsFalseForNotMatchingMethodNames() {
        val template = fakeCallTemplate(ArgMatcher.Equals(1), name = "call")
        val entry = fakeCallEntry(name = "calle", args = listOf(1))
        assertFalse(matcher.areMatching(template, entry))
    }

    @Test
    fun testAreMatchingReturnsFalseForNotMatchingSignature() {
        val template = fakeCallTemplate(ArgMatcher.Equals(1), ArgMatcher.Equals(1), functionId = CALL_OVERLOAD_ID)
        val entry = fakeCallEntry(name = "call", args = listOf(1))
        assertFalse(matcher.areMatching(template, entry))
    }

    @Test
    fun testAreMatchingReturnsFalseForNotMatchingArgsToSignature() {
        val template = fakeCallTemplate(ArgMatcher.Equals(1), name = "call")
        val entry = fakeCallEntry(name = "call", args = listOf(2))
        assertFalse(matcher.areMatching(template, entry))
    }

    @Test
    fun testAreMatchingReturnsFalseForNotMatchingReceivers() {
        val template = fakeCallTemplate(ArgMatcher.Equals(1), instanceId = 1, name = "call")
        val entry = fakeCallEntry(id = 2, name = "call", args = listOf(1))
        assertFalse(matcher.areMatching(template, entry))
    }

    @Test
    fun testAreMatchingReturnsFalseForNotSatisfiedMatcher() {
        val template = fakeCallTemplate(ArgMatcher.Equals(2), name = "call")
        val entry = fakeCallEntry(name = "call", args = listOf(1))
        assertFalse(matcher.areMatching(template, entry))
    }

    @Test
    fun testAreMatchingNeverResolvesMemberFunctions() {
        val template = fakeCallTemplate(ArgMatcher.Equals(1), name = "call")
        matcher.areMatching(template, fakeCallEntry(name = "call", args = listOf(1)))
        matcher.areMatching(template, fakeCallEntry(name = "calle", args = listOf(1)))
        matcher.areMatching(template, fakeCallEntry(name = "call", args = listOf(2)))
        matcher.areMatching(template, fakeCallEntry(id = 2, name = "call", args = listOf(1)))
        assertEquals(0, memberFunctions.lookups)
    }
}

private const val CALL_OVERLOAD_ID = -1L
