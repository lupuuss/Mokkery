package dev.mokkery.internal.answering

import dev.mokkery.MockMode.autoUnit
import dev.mokkery.MockMode.autofill
import dev.mokkery.MockMode.original
import dev.mokkery.MockMode.strict
import dev.mokkery.answering.Answer
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.internal.unsafeCast
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.ScopeCapturingAnswer
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.TestMokkeryInstance
import dev.mokkery.test.TestMokkeryInstanceLookup
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallContext
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeCallTrace
import dev.mokkery.test.runTest
import kotlin.reflect.KClass
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class AnsweringInterceptorTest {

    private val callMatcher = TestCallMatcher()
    private val lookUp = TestMokkeryInstanceLookup()
    private val answering = AnsweringInterceptor(strict, callMatcher, lookUp)

    @Test
    fun testThrowsCallNotMockedOnInterceptCallWhenNoAnswersAndStrictMode() {
        assertFailsWith<CallNotMockedException> {
            answering.interceptCall(fakeCallContext<Unit>())
        }
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptSuspendCallWhenNoAnswersAndStrictMode() = runTest {
        assertFailsWith<CallNotMockedException> {
            answering.interceptSuspendCall(fakeCallContext<Unit>())
        }
    }

    @Test
    fun testReturnsUnitOnInterceptCallWhenNoAnswersAndAutoUnitModeAndMethodReturnsUnit() {
        AnsweringInterceptor(autoUnit, callMatcher).interceptCall(fakeCallContext<Unit>())
    }

    @Test
    fun testReturnsUnitOnInterceptSuspendCallWhenNoAnswersAndAutoUnitModeAndMethodReturnsUnit() = runTest {
        AnsweringInterceptor(autoUnit, callMatcher).interceptSuspendCall(fakeCallContext<Unit>())
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptCallWhenNoAnswersAndAutoUnitModeAndMethodReturnsNotUnit() {
        assertFailsWith<CallNotMockedException> {
            AnsweringInterceptor(autoUnit, callMatcher).interceptCall(fakeCallContext<Int>())
        }
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptSuspendCallWhenNoAnswersAndAutoUnitModeAndMethodReturnsNotUnit() = runTest {
        assertFailsWith<CallNotMockedException> {
            AnsweringInterceptor(autoUnit, callMatcher).interceptSuspendCall(fakeCallContext<Int>())
        }
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptCallWhenNoSuperCallsForMockModeOriginal() {
        assertFailsWith<CallNotMockedException> {
            AnsweringInterceptor(original, callMatcher).interceptCall(fakeCallContext<Int>())
        }
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptSuspendCallWhenNoSuperCallsForMockModeOriginal() = runTest {
        assertFailsWith<CallNotMockedException> {
            AnsweringInterceptor(original, callMatcher).interceptSuspendCall(fakeCallContext<Int>())
        }
    }

    @Test
    fun testCallsOriginalOnInterceptCallWhenInterceptedTypeSuperCallPresentForMockModeOriginal() {
        val lookup = TestMokkeryInstanceLookup { TestMokkeryInstance(interceptedTypes = listOf(Unit::class)) }
        val context = fakeCallContext<Int>(supers = mapOf(Unit::class to { _: List<Any?> -> 10 }))
        assertEquals(10, AnsweringInterceptor(original, callMatcher, lookup).interceptCall(context))
    }

    @Test
    fun testCallsOriginalOnInterceptSuspendCallWhenInterceptedTypeSuperCallPresentForMockModeOriginal() = runTest {
        val lookup = TestMokkeryInstanceLookup { TestMokkeryInstance(interceptedTypes = listOf(Unit::class)) }
        val suspendSuper: suspend (List<Any?>) -> Any? = { 11 }
        val context = fakeCallContext<Int>(supers = mapOf(Unit::class to suspendSuper.unsafeCast()))
        assertEquals(11, AnsweringInterceptor(original, callMatcher, lookup).interceptSuspendCall(context))
    }

    @Test
    fun testReturnsEmptyValueOnInterceptCallWhenNoAnswersAndAutofillModeAndMethodReturnsNotUnit() {
        assertEquals(0, AnsweringInterceptor(autofill, callMatcher).interceptCall(fakeCallContext<Int>()))
    }

    @Test
    fun testReturnsEmptyValueOnInterceptSuspendCallWhenNoAnswersAndAutofillModeAndMethodReturnsNotUnit() {
        assertEquals(0, AnsweringInterceptor(autofill, callMatcher).interceptCall(fakeCallContext<Int>()))
    }

    @Test
    fun testReturnsAnswerOnInterceptCallForMatchingTemplate() {
        callMatcher.returns(true)
        answering.setup(fakeCallTemplate(), Answer.Const(3))
        assertEquals(3, answering.interceptCall(fakeCallContext<Int>()))
    }

    @Test
    fun testReturnsAnswerOnInterceptSuspendCallForMatchingTemplate() = runTest {
        callMatcher.returns(true)
        answering.setup(fakeCallTemplate(), Answer.Const(3))
        assertEquals(3, answering.interceptSuspendCall(fakeCallContext<Int>()))
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptCallForMissingMatchingTemplate() {
        callMatcher.returns(false)
        answering.setup(fakeCallTemplate(), Answer.Const(3))
        assertFailsWith<CallNotMockedException> {
            answering.interceptCall(fakeCallContext<Int>())
        }
    }

    @Test
    fun testThrowsCallNotMockedOnInterceptSuspendCallForMissingMatchingTemplate() = runTest {
        callMatcher.returns(false)
        answering.setup(fakeCallTemplate(), Answer.Const(3))
        assertFailsWith<CallNotMockedException> {
            answering.interceptSuspendCall(fakeCallContext<Int>())
        }
    }

    @Test
    fun testReturnsLatestAnswerOnInterceptCallWhenMoreThanOneMatching() {
        callMatcher.returns(true)
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(1))), Answer.Const(2))
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(2))), Answer.Const(3))
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(3))), Answer.Const(4))
        assertEquals(4, answering.interceptCall(fakeCallContext<Int>()))
    }

    @Test
    fun testReturnsLatestAnswerOnInterceptSuspendCallWhenMoreThanOneMatching() = runTest {
        callMatcher.returns(true)
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(1))), Answer.Const(2))
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(2))), Answer.Const(3))
        answering.setup(fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(3))), Answer.Const(4))
        assertEquals(4, answering.interceptSuspendCall(fakeCallContext<Int>()))
    }

    @Test
    fun testCallMatcherIsCalledCorrectlyOnInterceptCall() {
        callMatcher.returnsMany(true, true, true)
        val template1 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(1)))
        val template2 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(2)))
        val template3 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(3)))
        answering.setup(template1, Answer.Const(2))
        answering.setup(template2, Answer.Const(3))
        answering.setup(template3, Answer.Const(4))
        answering.interceptCall(fakeCallContext<Int>())
        assertEquals(
            listOf(template3),
            callMatcher.recordedCalls.map { it.second }
        )
    }

    @Test
    fun testCallMatcherIsCalledCorrectlyOnInterceptSuspendCall() = runTest {
        callMatcher.returnsMany(true, true, true)
        val template1 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(1)))
        val template2 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(2)))
        val template3 = fakeCallTemplate(matchers = mapOf("i" to ArgMatcher.Equals(3)))
        answering.setup(template1, Answer.Const(2))
        answering.setup(template2, Answer.Const(3))
        answering.setup(template3, Answer.Const(4))
        val context = fakeCallContext<Int>(
            selfId = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "1", value = 1))
        )
        val expectedTrace = fakeCallTrace(
            receiver = "mock@1",
            name = "call",
            args = listOf(fakeCallArg(name = "1", value = 1)),
            orderStamp = 0
        )
        answering.interceptSuspendCall(context)
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
        val context = fakeCallContext<Int>(
            args = listOf(1, 2, 3).map { CallArg("<$it>", Int::class, it, false) },
            supers = mapOf<KClass<*>, (List<Any?>) -> Any?>(Unit::class to {  })
        )
        answering.interceptCall(context)
        assertNotNull(answer.capturedScope)
        assertEquals(context.args.map(CallArg::value), answer.capturedScope!!.args)
        assertEquals(context.instance, answer.capturedScope!!.self)
        assertEquals(context.returnType, answer.capturedScope!!.returnType)
        assertEquals(context.supers, answer.capturedScope!!.supers)
    }

    @Test
    fun testPassesCorrectParamsToAnswerOnInterceptSuspendCall() = runTest {
        callMatcher.returns(true)
        val answer = ScopeCapturingAnswer()
        answering.setup(fakeCallTemplate(), answer)
        val context = fakeCallContext<Int>(
            args = listOf(1, 2, 3).map { CallArg("<$it>", Int::class, it, false) },
            supers = mapOf<KClass<*>, (List<Any?>) -> Any?>(Unit::class to {  })
        )
        answering.interceptSuspendCall(context)
        assertNotNull(answer.capturedScope)
        assertEquals(context.args.map(CallArg::value), answer.capturedScope!!.args)
        assertEquals(context.instance, answer.capturedScope!!.self)
        assertEquals(context.returnType, answer.capturedScope!!.returnType)
        assertEquals(context.supers, answer.capturedScope!!.supers)
    }
}
