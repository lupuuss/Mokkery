package dev.mokkery.internal.answering

import dev.mokkery.MockMode.autoUnit
import dev.mokkery.MockMode.autofill
import dev.mokkery.MockMode.strict
import dev.mokkery.answering.Answer
import dev.mokkery.answering.FunctionScope
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.tracing.CallArg
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.fakeCallContext
import dev.mokkery.test.fakeCallTemplate
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class AnsweringInterceptorTest {

    private val callMatcher = TestCallMatcher()
    private val answering = AnsweringInterceptor(strict, callMatcher)

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
    fun testReturnsEmptyValueOnInterceptCallWhenNoAnswersAndAutofillModeAndMethodReturnsNotUnit() {
        assertEquals(0, AnsweringInterceptor(autofill, callMatcher).interceptCall(fakeCallContext<Int>()))
    }

    @Test
    fun testReturnsEmptyValueOnInterceptSuspendCallWhenNoAnswersAndAutofillModeAndMethodReturnsNotUnit() = runTest {
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
            listOf(template1, template2, template3),
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
        answering.interceptSuspendCall(fakeCallContext<Int>())
        assertEquals(
            listOf(template1, template2, template3),
            callMatcher.recordedCalls.map { it.second }
        )
    }

    @Test
    fun testPassesCorrectParamsToAnswerOnInterceptCall() {
        callMatcher.returns(true)
        var capturedFunctionScope: FunctionScope? = null
        answering.setup(fakeCallTemplate(), Answer.Block { capturedFunctionScope = it })
        val context = fakeCallContext<Int>()
        answering.interceptCall(context)
        assertNotNull(capturedFunctionScope)
        assertEquals(context.args.map(CallArg::value), capturedFunctionScope!!.args)
        assertEquals(context.self, capturedFunctionScope!!.self)
        assertEquals(context.returnType, capturedFunctionScope!!.returnType)
    }

    @Test
    fun testPassesCorrectParamsToAnswerOnInterceptSuspendCall() = runTest {
        callMatcher.returns(true)
        var capturedFunctionScope: FunctionScope? = null
        answering.setup(fakeCallTemplate(), Answer.Block { capturedFunctionScope = it })
        val context = fakeCallContext<Int>()
        answering.interceptSuspendCall(context)
        assertNotNull(capturedFunctionScope)
        assertEquals(context.args.map(CallArg::value), capturedFunctionScope!!.args)
        assertEquals(context.self, capturedFunctionScope!!.self)
        assertEquals(context.returnType, capturedFunctionScope!!.returnType)
    }
}
