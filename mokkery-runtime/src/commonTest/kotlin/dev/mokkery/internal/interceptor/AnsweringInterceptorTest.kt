package dev.mokkery.internal.interceptor

import dev.mokkery.MockMode
import dev.mokkery.MockMode.autoUnit
import dev.mokkery.MockMode.autofill
import dev.mokkery.MockMode.original
import dev.mokkery.answering.Answer
import dev.mokkery.context.CallArgument
import dev.mokkery.interceptor.call
import dev.mokkery.interceptor.self
import dev.mokkery.interceptor.supers
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.context.MokkeryTools
import dev.mokkery.internal.context.currentMockContext
import dev.mokkery.internal.utils.unsafeCast
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.ScopeCapturingAnswer
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.TestCallTraceReceiverShortener
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.TestMokkeryScopeLookup
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.runTest
import dev.mokkery.test.testBlockingCallScope
import dev.mokkery.test.testSuspendCallScope
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class AnsweringInterceptorTest {

    private val callMatcher = TestCallMatcher()
    private val tools = MokkeryTools(
        callMatcher = callMatcher,
        callTraceReceiverShortener = TestCallTraceReceiverShortener(),
        instanceLookup = TestMokkeryScopeLookup()
    )
    private val context = tools + currentMockContext(MockMode.strict)
    private val answering = AnsweringInterceptor()

    @Test
    fun testThrowsCallNotMockedOnInterceptCallWhenNoAnswersAndStrictMode() {
        assertFailsWith<CallNotMockedException> {
            answering.intercept(testBlockingCallScope<Unit>(context = context))
        }
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptSuspendCallWhenNoAnswersAndStrictMode() = runTest {
        assertFailsWith<CallNotMockedException> {
            answering.intercept(testSuspendCallScope<Unit>(context = context))
        }
    }

    @Test
    fun testReturnsUnitOnInterceptCallWhenNoAnswersAndAutoUnitModeAndMethodReturnsUnit() {
        answering.intercept(testBlockingCallScope<Unit>(context = context + currentMockContext(autoUnit)))
    }

    @Test
    fun testReturnsUnitOnInterceptSuspendCallWhenNoAnswersAndAutoUnitModeAndMethodReturnsUnit() = runTest {
        answering.intercept(testSuspendCallScope<Unit>(context = context + currentMockContext(autoUnit)))
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptCallWhenNoAnswersAndAutoUnitModeAndMethodReturnsNotUnit() {
        assertFailsWith<CallNotMockedException> {
            answering.intercept(testBlockingCallScope<Int>(context = context + currentMockContext(autoUnit)))
        }
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptSuspendCallWhenNoAnswersAndAutoUnitModeAndMethodReturnsNotUnit() = runTest {
        assertFailsWith<CallNotMockedException> {
            answering.intercept(testSuspendCallScope<Int>(context = context + currentMockContext(autoUnit)))
        }
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptCallWhenNoSuperCallsForMockModeOriginal() {
        assertFailsWith<CallNotMockedException> {
            answering.intercept(testBlockingCallScope<Int>(context = context + currentMockContext(original)))
        }
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptSuspendCallWhenNoSuperCallsForMockModeOriginal() = runTest {
        assertFailsWith<CallNotMockedException> {
            answering.intercept(testSuspendCallScope<Int>(context = context + currentMockContext(original)))
        }
    }

    @Test
    fun testCallsOriginalOnInterceptCallWhenInterceptedTypeSuperCallPresentForMockModeOriginal() {
        val lookUp = TestMokkeryScopeLookup { TestMokkeryInstanceScope(interceptedTypes = listOf(Unit::class)) }
        val scope = testBlockingCallScope<Int>(
            supers = mapOf(Unit::class to { _: List<Any?> -> 10 }),
            context = context + tools.copy(instanceLookup = lookUp) + currentMockContext(original)
        )
        assertEquals(10, answering.intercept(scope))
    }

    @Test
    fun testCallsOriginalOnInterceptSuspendCallWhenInterceptedTypeSuperCallPresentForMockModeOriginal() = runTest {
        val lookUp = TestMokkeryScopeLookup { TestMokkeryInstanceScope(interceptedTypes = listOf(Unit::class)) }
        val suspendSuper: suspend (List<Any?>) -> Any? = { 11 }
        val scope = testSuspendCallScope<Int>(
            supers = mapOf(Unit::class to suspendSuper.unsafeCast()),
            context = context + tools.copy(instanceLookup = lookUp) + currentMockContext(original)
        )
        assertEquals(11, answering.intercept(scope))
    }

    @Test
    fun testReturnsEmptyValueOnInterceptCallWhenNoAnswersAndAutofillModeAndMethodReturnsNotUnit() {
        assertEquals(
            expected = 0,
            actual = answering.intercept(testBlockingCallScope<Int>(context = context + currentMockContext(autofill)))
        )
    }

    @Test
    fun testReturnsEmptyValueOnInterceptSuspendCallWhenNoAnswersAndAutofillModeAndMethodReturnsNotUnit() {
        assertEquals(
            0,
            answering.intercept(testBlockingCallScope<Int>(context = context + currentMockContext(autofill)))
        )
    }

    @Test
    fun testReturnsAnswerOnInterceptCallForMatchingTemplate() {
        callMatcher.returns(true)
        answering.setup(fakeCallTemplate(), Answer.Const(3))
        assertEquals(3, answering.intercept(testBlockingCallScope<Int>(context = context)))
    }

    @Test
    fun testReturnsAnswerOnInterceptSuspendCallForMatchingTemplate() = runTest {
        callMatcher.returns(true)
        answering.setup(fakeCallTemplate(), Answer.Const(3))
        assertEquals(3, answering.intercept(testSuspendCallScope<Int>(context = context)))
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptCallForMissingMatchingTemplate() {
        callMatcher.returns(false)
        answering.setup(fakeCallTemplate(), Answer.Const(3))
        assertFailsWith<CallNotMockedException> {
            answering.intercept(testBlockingCallScope<Int>(context = context))
        }
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptSuspendCallForMissingMatchingTemplate() = runTest {
        callMatcher.returns(false)
        answering.setup(fakeCallTemplate(), Answer.Const(3))
        assertFailsWith<CallNotMockedException> {
            answering.intercept(testSuspendCallScope<Int>(context = context))
        }
    }

    @Test
    fun testReturnsLatestAnswerOnInterceptCallWhenMoreThanOneMatching() {
        callMatcher.returns(true)
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(1))), Answer.Const(2))
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(2))), Answer.Const(3))
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(3))), Answer.Const(4))
        assertEquals(4, answering.intercept(testBlockingCallScope<Int>(context = context)))
    }

    @Test
    fun testReturnsLatestAnswerOnInterceptSuspendCallWhenMoreThanOneMatching() = runTest {
        callMatcher.returns(true)
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(1))), Answer.Const(2))
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(2))), Answer.Const(3))
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(3))), Answer.Const(4))
        assertEquals(4, answering.intercept(testSuspendCallScope<Int>(context = context)))
    }

    @Test
    fun testCallMatcherIsCalledCorrectlyOnIntercept() {
        callMatcher.returnsMany(true, true, true)
        val template1 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(1)))
        val template2 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(2)))
        val template3 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(3)))
        answering.setup(template1, Answer.Const(2))
        answering.setup(template2, Answer.Const(3))
        answering.setup(template3, Answer.Const(4))
        answering.intercept(testBlockingCallScope<Int>(context = context))
        assertEquals(
            listOf(template3),
            callMatcher.recordedCalls.map { it.second }
        )
    }

    @Test
    fun testCallMatcherIsCalledCorrectlyOnInterceptSuspend() = runTest {
        callMatcher.returnsMany(true, true, true)
        val template1 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(1)))
        val template2 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(2)))
        val template3 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(3)))
        answering.setup(template1, Answer.Const(2))
        answering.setup(template2, Answer.Const(3))
        answering.setup(template3, Answer.Const(4))
        val scope = testSuspendCallScope<Int>(
            selfId = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "1", value = 1)),
            context = context
        )
        val expectedTrace = fakeCallTrace(
            receiver = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "1", value = 1)),
            orderStamp = 0
        )
        answering.intercept(scope)
        assertEquals(
            listOf(template3),
            callMatcher.recordedCalls.map { it.second }
        )
        assertEquals(
            listOf(expectedTrace),
            callMatcher.recordedCalls.map { it.first }
        )
    }

    @Test
    fun testPassesCorrectParamsToAnswerOnInterceptCall() {
        callMatcher.returns(true)
        val answer = ScopeCapturingAnswer()
        answering.setup(fakeCallTemplate(), answer)
        val scope = testBlockingCallScope<Int>(
            args = listOf(1, 2, 3).map { CallArgument(it, "<$it>", Int::class, false) },
            supers = mapOf<KClass<*>, (List<Any?>) -> Any?>(Unit::class to { }),
            context = context
        )
        answering.intercept(scope)
        assertNotNull(answer.capturedScope)
        assertEquals(scope.call.args.map(CallArgument::value), answer.capturedScope!!.args)
        assertEquals(scope.self, answer.capturedScope!!.self)
        assertEquals(scope.call.function.returnType, answer.capturedScope!!.returnType)
        assertEquals(scope.supers, answer.capturedScope!!.supers)
    }

    @Test
    fun testPassesCorrectParamsToAnswerOnInterceptSuspendCall() = runTest {
        callMatcher.returns(true)
        val answer = ScopeCapturingAnswer()
        answering.setup(fakeCallTemplate(), answer)
        val scope = testSuspendCallScope<Int>(
            args = listOf(1, 2, 3).map { CallArgument(it, "<$it>", Int::class, false) },
            supers = mapOf<KClass<*>, (List<Any?>) -> Any?>(Unit::class to { }),
            context = context
        )
        answering.intercept(scope)
        assertNotNull(answer.capturedScope)
        assertEquals(scope.call.args.map(CallArgument::value), answer.capturedScope!!.args)
        assertEquals(scope.self, answer.capturedScope!!.self)
        assertEquals(scope.call.function.returnType, answer.capturedScope!!.returnType)
        assertEquals(scope.supers, answer.capturedScope!!.supers)
    }

    private fun currentMockContext(mode: MockMode) = TestMokkeryInstanceScope(mode = mode).currentMockContext
}
