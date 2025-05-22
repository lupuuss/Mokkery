package dev.mokkery.internal.answering

import dev.mokkery.MockMode
import dev.mokkery.answering.Answer
import dev.mokkery.answering.SuperCall
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.context.MokkeryTools
import dev.mokkery.internal.context.mockSpec
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.TestCallTraceReceiverShortener
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.testBlockingCallScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AnsweringRegistryTest {

    private val callMatcher = TestCallMatcher()
    private val tools = MokkeryTools(
        callMatcher = callMatcher,
        callTraceReceiverShortener = TestCallTraceReceiverShortener()
    )
    private val context = tools + currentMockContext(MockMode.strict)
    private val answering = AnsweringRegistry()

    @Test
    fun testResolveAnswerThrowsCallNotMockedOnWhenNoAnswersAndStrictMode() {
        assertFailsWith<CallNotMockedException> {
            answering.resolveAnswer(testBlockingCallScope<Unit>(context = context))
        }
    }

    @Test
    fun testResolveAnswerReturnsConstUnitAnswerWhenNoAnswersAndAutoUnitModeAndMethodReturnsUnit() {
        val scope = testBlockingCallScope<Unit>(context = context + currentMockContext(MockMode.autoUnit))
        val answer = answering.resolveAnswer(scope)
        assertEquals(Answer.Const(Unit), answer)
    }

    @Test
    fun testResolveAnswerThrowsCallNotMockedWhenNoAnswersAndAutoUnitModeAndMethodReturnsNotUnit() {
        assertFailsWith<CallNotMockedException> {
            val scope = testBlockingCallScope<Int>(context = context + currentMockContext(MockMode.autoUnit))
            answering.resolveAnswer(scope)
        }
    }

    @Test
    fun testResolveAnswerThrowsCallNotMockedWhenNoSuperCallsForMockModeOriginal() {
        assertFailsWith<CallNotMockedException> {
            val scope = testBlockingCallScope<Int>(context = context + currentMockContext(MockMode.original))
            answering.resolveAnswer(scope)
        }
    }

    @Test
    fun testResolveAnswerReturnsSuperCallAnswerWithOriginalWhenInterceptedTypeSuperCallPresentForMockModeOriginal() {
        val scope = testBlockingCallScope<Int>(
            supers = mapOf(Unit::class to { _: List<Any?> -> 10 }),
            context = context + tools + currentMockContext(MockMode.original)
        )
        assertEquals(SuperCallAnswer<Any?>(SuperCall.original), answering.resolveAnswer(scope))
    }

    @Test
    fun testResolveAnswerReturnsAutofillAnswerWhenNoAnswersAndAutofillModeAndMethodReturnsNotUnit() {
        val scope = testBlockingCallScope<Int>(context = context + currentMockContext(MockMode.autofill))
        val answer = answering.resolveAnswer(scope)
        assertEquals(Answer.Autofill, answer)
    }

    @Test
    fun testResolveAnswerReturnsAnswerForMatchingTemplate() {
        callMatcher.returns(true)
        answering.setup(fakeCallTemplate(), Answer.Const(3))
        assertEquals(Answer.Const(3), answering.resolveAnswer(testBlockingCallScope<Int>(context = context)))
    }


    @Test
    fun testResolveAnswerThrowsCallNotMockedOForMissingMatchingTemplate() {
        callMatcher.returns(false)
        answering.setup(fakeCallTemplate(), Answer.Const(3))
        assertFailsWith<CallNotMockedException> {
            answering.resolveAnswer(testBlockingCallScope<Int>(context = context))
        }
    }

    @Test
    fun testResolveAnswerReturnsLatestAnswerOnInterceptCallWhenMoreThanOneMatching() {
        callMatcher.returns(true)
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(1))), Answer.Const(2))
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(2))), Answer.Const(3))
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(3))), Answer.Const(4))
        assertEquals(Answer.Const(4), answering.resolveAnswer(testBlockingCallScope<Int>(context = context)))
    }

    @Test
    fun testResolveAnswerCallsCallMatcherCorrectly() {
        callMatcher.returnsMany(true, true, true)
        val template1 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(1)))
        val template2 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(2)))
        val template3 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(3)))
        answering.setup(template1, Answer.Const(2))
        answering.setup(template2, Answer.Const(3))
        answering.setup(template3, Answer.Const(4))
        val scope = testBlockingCallScope<Int>(
            typeName = "mock",
            sequence = 1,
            name = "call",
            args = listOf(fakeCallArg(name = "1", value = 1)),
            context = context
        )
        answering.resolveAnswer(scope)
        val expectedTrace = fakeCallTrace(
            name = "call",
            args = listOf(fakeCallArg(name = "1", value = 1)),
            orderStamp = 0
        )
        assertEquals(listOf(template3), callMatcher.recordedCalls.map { it.second })
        assertEquals(listOf(expectedTrace), callMatcher.recordedCalls.map { it.first })
    }

    private fun currentMockContext(mode: MockMode) = TestMokkeryInstanceScope(mode = mode).mockSpec
}
