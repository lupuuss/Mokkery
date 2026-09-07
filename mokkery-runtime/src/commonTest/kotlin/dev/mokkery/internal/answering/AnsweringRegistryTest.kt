package dev.mokkery.internal.answering

import dev.mokkery.MockMode
import dev.mokkery.answering.Answer
import dev.mokkery.answering.SuperCall
import dev.mokkery.internal.CallNotMockedException
import dev.mokkery.internal.context.MokkeryTools
import dev.mokkery.internal.context.instanceSpec
import dev.mokkery.matcher.ArgMatcher
import dev.mokkery.matcher.capture.CaptureMatcher
import dev.mokkery.matcher.capture.asCapture
import dev.mokkery.test.TestCallMatcher
import dev.mokkery.test.TestInstanceContracts
import dev.mokkery.test.TestMokkeryInstanceScope
import dev.mokkery.test.TestNameShortener
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallEntry
import dev.mokkery.test.fakeCallTemplate
import dev.mokkery.test.fakeFunParam
import dev.mokkery.test.fakeFunction
import dev.mokkery.test.testBlockingCallScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AnsweringRegistryTest {

    private val function = fakeFunction("call", parameters = listOf(fakeFunParam<Int>("i")))
    private val args = listOf(fakeCallArg(value = 1, name = "i"))
    private val callMatcher = TestCallMatcher()
    private val tools = MokkeryTools(namesShortener = TestNameShortener())
    private val context = tools + mockSpec(MockMode.strict) + callMatcher
    private val answering = AnsweringRegistry()

    @Test
    fun testResolveAnswerThrowsCallNotMockedOnWhenNoAnswersAndStrictMode() {
        assertFailsWith<CallNotMockedException> {
            answering.resolveAnswer(testBlockingCallScope<Unit>(args = args, context = context))
        }
    }

    @Test
    fun testResolveAnswerReturnsConstUnitAnswerWhenNoAnswersAndAutoUnitModeAndMethodReturnsUnit() {
        val scope = testBlockingCallScope<Unit>(args = args, context = context + mockSpec(MockMode.autoUnit))
        val answer = answering.resolveAnswer(scope)
        assertEquals(Answer.Const(Unit), answer)
    }

    @Test
    fun testResolveAnswerThrowsCallNotMockedWhenNoAnswersAndAutoUnitModeAndMethodReturnsNotUnit() {
        assertFailsWith<CallNotMockedException> {
            val scope = testBlockingCallScope<Int>(args = args, context = context + mockSpec(MockMode.autoUnit))
            answering.resolveAnswer(scope)
        }
    }

    @Test
    fun testResolveAnswerThrowsCallNotMockedWhenNoSuperCallsForMockModeOriginal() {
        assertFailsWith<CallNotMockedException> {
            val scope = testBlockingCallScope<Int>(
                args = args,
                context = context + mockSpec(MockMode.original) + TestInstanceContracts(functionId = function.id.value)
            )
            answering.resolveAnswer(scope)
        }
    }

    @Test
    fun testResolveAnswerReturnsSuperCallAnswerWithOriginalWhenInterceptedTypeSuperCallPresentForMockModeOriginal() {
        val scope = testBlockingCallScope<Int>(
            args = args,
            context = context
                    + tools
                    + mockSpec(MockMode.original)
                    + TestInstanceContracts(functionId = function.id.value, supers = mapOf(Unit::class to { _: List<Any?> -> 10 }))
        )
        assertEquals(SuperCallAnswer<Any?>(SuperCall.original), answering.resolveAnswer(scope))
    }

    @Test
    fun testResolveAnswerReturnsAutofillAnswerWhenNoAnswersAndAutofillModeAndMethodReturnsNotUnit() {
        val scope = testBlockingCallScope<Int>(args = args, context = context + mockSpec(MockMode.autofill))
        val answer = answering.resolveAnswer(scope)
        assertEquals(Answer.Autofill, answer)
    }

    @Test
    fun testResolveAnswerReturnsAnswerForMatchingTemplate() {
        callMatcher.returns(true)
        answering.setup(fakeCallTemplate(), Answer.Const(3))
        assertEquals(Answer.Const(3), answering.resolveAnswer(testBlockingCallScope<Int>(args = args, context = context)))
    }


    @Test
    fun testResolveAnswerThrowsCallNotMockedOForMissingMatchingTemplate() {
        callMatcher.returns(false)
        answering.setup(fakeCallTemplate(), Answer.Const(3))
        assertFailsWith<CallNotMockedException> {
            answering.resolveAnswer(testBlockingCallScope<Int>(args = args, context = context))
        }
    }

    @Test
    fun testResolveAnswerReturnsLatestAnswerOnInterceptCallWhenMoreThanOneMatching() {
        callMatcher.returns(true)
        answering.setup(fakeCallTemplate(ArgMatcher.Equals(1)), Answer.Const(2))
        answering.setup(fakeCallTemplate(ArgMatcher.Equals(2)), Answer.Const(3))
        answering.setup(fakeCallTemplate(ArgMatcher.Equals(3)), Answer.Const(4))
        assertEquals(Answer.Const(4), answering.resolveAnswer(testBlockingCallScope<Int>(args = args, context = context)))
    }

    @Test
    fun testResolveAnswerCallsCallMatcherCorrectly() {
        callMatcher.returnsMany(true, true, true)
        val template1 = fakeCallTemplate(ArgMatcher.Equals(1))
        val template2 = fakeCallTemplate(ArgMatcher.Equals(2))
        val template3 = fakeCallTemplate(ArgMatcher.Equals(3))
        answering.setup(template1, Answer.Const(2))
        answering.setup(template2, Answer.Const(3))
        answering.setup(template3, Answer.Const(4))
        val scope = testBlockingCallScope<Int>(
            instanceId = 1,
            typeName = "mock",
            name = "call",
            args = listOf(fakeCallArg(value = 1, name = "1")),
            context = context
        )
        answering.resolveAnswer(scope)
        val expectedEntry = fakeCallEntry(name = "call", args = listOf(1))
        assertEquals(listOf(template3), callMatcher.recordedCalls.map { it.first })
        assertEquals(listOf(expectedEntry), callMatcher.recordedCalls.map { it.second })
    }

    @Test
    fun testResolveAnswerAppliesCaptureForMatchingTemplate() {
        callMatcher.returns(true)
        val captured = mutableListOf<Any?>()
        answering.setup(fakeCallTemplate(CaptureMatcher(captured.asCapture(), ArgMatcher.Any)), Answer.Const(1))
        val scope = testBlockingCallScope<Int>(
            args = listOf(fakeCallArg(value = 1, name = "i")),
            context = context
        )
        answering.resolveAnswer(scope)
        assertEquals(listOf<Any?>(1), captured)
    }

    private fun mockSpec(mode: MockMode) = TestMokkeryInstanceScope(mode = mode, functions = listOf(function)).instanceSpec
}
