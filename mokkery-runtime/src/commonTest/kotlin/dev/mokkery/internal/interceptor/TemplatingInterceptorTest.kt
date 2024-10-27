package dev.mokkery.internal.interceptor

import dev.mokkery.answering.autofill.AutofillProvider
import dev.mokkery.internal.context.asContext
import dev.mokkery.test.TestNextCallInterceptor
import dev.mokkery.test.TestTemplatingScope
import dev.mokkery.test.TestTemplatingScope.TemplateParams
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.runTest
import dev.mokkery.test.testBlockingCallScope
import dev.mokkery.test.testSuspendCallScope
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TemplatingInterceptorTest {

    private val nextInterceptor = TestNextCallInterceptor()
    private val autofillProvider = AutofillProvider.ofNotNull {
        when (it) {
            Int::class -> 13
            String::class -> "Hello!"
            else -> Unit
        }
    }
    private val context = nextInterceptor + autofillProvider.asContext()
    private val templatingScope = TestTemplatingScope()
    private val templating = TemplatingInterceptor()
    private val args = listOf(
        fakeCallArg(name = "i", value = 1),
        fakeCallArg(name = "j", value = 1)
    )

    @Test
    fun testCallsNextInterceptorOnInterceptCallWhenNotEnabled() {
        val scope = testBlockingCallScope<Int>(context = context)
        templating.intercept(scope)
        assertEquals(listOf(scope), nextInterceptor.interceptedCalls)
        assertEquals(emptyList(), nextInterceptor.interceptedSuspendCalls)
    }

    @Test
    fun testCallsNextInterceptorOnInterceptSuspendCallWhenNotEnabled() = runTest {
        val scope = testSuspendCallScope<Int>(context = context)
        templating.intercept(scope)
        assertEquals(emptyList(), nextInterceptor.interceptedCalls)
        assertEquals(listOf(scope), nextInterceptor.interceptedSuspendCalls)
    }

    @Test
    fun testReturnsAutofillValueOnInterceptCallWhenEnabled() {
        templating.start(templatingScope)
        assertEquals(13, templating.intercept(testBlockingCallScope<Int>(context = context)))
    }

    @Test
    fun testReturnsAutofillValueOnInterceptSuspendCallWhenEnabled() = runTest {
        templating.start(templatingScope)
        assertEquals(13, templating.intercept(testSuspendCallScope<Int>(context = context)))
    }

    @Test
    fun testReturnAutofillValueForTypeHintOnInterceptCallWhenEnabled() {
        templating.start(templatingScope)
        templatingScope.currentGenericReturnTypeHint = String::class
        assertEquals("Hello!", templating.intercept(testBlockingCallScope<Int>(context = context)))
    }

    @Test
    fun testReturnsAutofillValueForTypeHintOnInterceptSuspendCallWhenEnabled() = runTest {
        templating.start(templatingScope)
        templatingScope.currentGenericReturnTypeHint = String::class
        assertEquals("Hello!", templating.intercept(testSuspendCallScope<Int>(context = context)))
    }

    @Test
    fun testDoesNotRegisterCallsOnInterceptCallWhenNotEnabled() {
        templating.intercept(testBlockingCallScope<Int>(context = nextInterceptor))
        assertEquals(emptyList(), templatingScope.recordedSaveCalls)
    }

    @Test
    fun testDoesNotRegisterCallsOnInterceptSuspendCallWhenNotEnabled() = runTest {
        templating.intercept(testSuspendCallScope<Int>(context = nextInterceptor))
        assertEquals(emptyList(), templatingScope.recordedSaveCalls)
    }

    @Test
    fun testRegisterCallsOnInterceptCallWhenNotEnabled() {
        templating.start(templatingScope)
        templating.intercept(
            testBlockingCallScope<Int>(
                selfId = "mock@1",
                name = "call1",
                args = args,
                context = context
            )
        )
        templating.intercept(
            testBlockingCallScope<Int>(
                selfId = "mock@1",
                name = "call2",
                args = args,
                context = context
            )
        )
        assertEquals(
            listOf(
                TemplateParams("mock@1", "call1", args),
                TemplateParams("mock@1", "call2", args)
            ),
            templatingScope.recordedSaveCalls
        )
    }

    @Test
    fun testRegisterCallsOnInterceptSuspendCallWhenNotEnabled() = runTest {
        templating.start(templatingScope)
        templating.intercept(
            testSuspendCallScope<Int>(
                selfId = "mock@1",
                name = "call1",
                args = args,
                context = context
            )
        )
        templating.intercept(
            testSuspendCallScope<Int>(
                selfId = "mock@1",
                name = "call2",
                args = args,
                context = context
            )
        )
        assertEquals(
            listOf(
                TemplateParams("mock@1", "call1", args),
                TemplateParams("mock@1", "call2", args)
            ),
            templatingScope.recordedSaveCalls
        )
    }

    @Test
    fun testIsEnabledReturnsTrueAfterStart() {
        templating.start(templatingScope)
        assertTrue(templating.isEnabled)
    }

    @Test
    fun testIsEnabledReturnsFalseAfterStop() {
        templating.start(templatingScope)
        templating.stop()
        assertFalse(templating.isEnabled)
    }

    @Test
    fun testIsNotEnabledAtStart() {
        assertFalse(templating.isEnabled)
    }

    @Test
    fun testIsEnabledWithReturnsTrueWhenScopeMatches() {
        templating.start(templatingScope)
        assertTrue(templating.isEnabledWith(templatingScope))
    }

    @Test
    fun testIsEnabledWithReturnsFalseWhenScopeDoesNotMatch() {
        templating.start(templatingScope)
        assertFalse(templating.isEnabledWith(TestTemplatingScope()))
    }

    @Test
    fun testIsEnabledWithReturnsFalseAfterStop() {
        templating.start(templatingScope)
        templating.stop()
        assertFalse(templating.isEnabledWith(templatingScope))
    }
}
