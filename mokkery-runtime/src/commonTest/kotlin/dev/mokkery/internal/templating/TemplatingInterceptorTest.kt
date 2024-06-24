package dev.mokkery.internal.templating

import dev.mokkery.internal.MokkeryToken
import dev.mokkery.test.TestTemplatingScope
import dev.mokkery.test.TestTemplatingScope.TemplateParams
import dev.mokkery.test.fakeCallArg
import dev.mokkery.test.fakeCallContext
import dev.mokkery.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TemplatingInterceptorTest {

    private val scope = TestTemplatingScope()
    private val templating = TemplatingInterceptor()
    private val args = listOf(
        fakeCallArg(name = "i", value = 1),
        fakeCallArg(name = "j", value = 1)
    )

    @Test
    fun testReturnsCallNextTokenOnInterceptCallWhenNotEnabled() {
        assertEquals(MokkeryToken.CallNext, templating.interceptCall(fakeCallContext<Int>()))
    }

    @Test
    fun testReturnsCallNextTokenOnInterceptSuspendCallWhenNotEnabled() = runTest {
        assertEquals(MokkeryToken.CallNext, templating.interceptSuspendCall(fakeCallContext<Int>()))
    }

    @Test
    fun testReturnsReturnDefaultTokenOnInterceptCallWhenEnabled() {
        templating.start(scope)
        assertEquals(MokkeryToken.ReturnDefault(null), templating.interceptCall(fakeCallContext<Int>()))
    }

    @Test
    fun testReturnsReturnDefaultTokenOnInterceptSuspendCallWhenEnabled() = runTest {
        templating.start(scope)
        assertEquals(MokkeryToken.ReturnDefault(null), templating.interceptSuspendCall(fakeCallContext<Int>()))
    }

    @Test
    fun testReturnsReturnDefaultTokenWithTypeHintOnInterceptCallWhenEnabled() {
        templating.start(scope)
        scope.currentGenericReturnTypeHint = Int::class
        assertEquals(MokkeryToken.ReturnDefault(Int::class), templating.interceptCall(fakeCallContext<Int>()))
    }

    @Test
    fun testReturnsReturnDefaultTokenWithTypeHintOnInterceptSuspendCallWhenEnabled() = runTest {
        templating.start(scope)
        scope.currentGenericReturnTypeHint = String::class
        assertEquals(MokkeryToken.ReturnDefault(String::class), templating.interceptSuspendCall(fakeCallContext<Int>()))
    }

    @Test
    fun testDoesNotRegisterCallsOnInterceptCallWhenNotEnabled() {
        templating.interceptCall(fakeCallContext<Int>())
        assertEquals(emptyList(), scope.recordedSaveCalls)
    }

    @Test
    fun testDoesNotRegisterCallsOnInterceptSuspendCallWhenNotEnabled() = runTest {
        templating.interceptSuspendCall(fakeCallContext<Int>())
        assertEquals(emptyList(), scope.recordedSaveCalls)
    }

    @Test
    fun testRegisterCallsOnInterceptCallWhenNotEnabled() {
        templating.start(scope)
        templating.interceptCall(fakeCallContext<Int>(selfId = "mock@1", name = "call1", args = args))
        templating.interceptCall(fakeCallContext<Int>(selfId = "mock@1", name = "call2", args = args))
        assertEquals(
            listOf(
                TemplateParams("mock@1", "call1", args),
                TemplateParams("mock@1", "call2", args)
            ),
            scope.recordedSaveCalls
        )
    }

    @Test
    fun testRegisterCallsOnInterceptSuspendCallWhenNotEnabled() = runTest {
        templating.start(scope)
        templating.interceptSuspendCall(fakeCallContext<Int>(selfId = "mock@1", name = "call1", args = args))
        templating.interceptSuspendCall(fakeCallContext<Int>(selfId = "mock@1", name = "call2", args = args))
        assertEquals(
            listOf(
                TemplateParams("mock@1", "call1", args),
                TemplateParams("mock@1", "call2", args)
            ),
            scope.recordedSaveCalls
        )
    }

    @Test
    fun testIsEnabledReturnsTrueAfterStart() {
        templating.start(scope)
        assertTrue(templating.isEnabled)
    }

    @Test
    fun testIsEnabledReturnsFalseAfterStop() {
        templating.start(scope)
        templating.stop()
        assertFalse(templating.isEnabled)
    }

    @Test
    fun testIsNotEnabledAtStart() {
        assertFalse(templating.isEnabled)
    }

    @Test
    fun testIsEnabledWithReturnsTrueWhenScopeMatches() {
        templating.start(scope)
        assertTrue(templating.isEnabledWith(scope))
    }

    @Test
    fun testIsEnabledWithReturnsFalseWhenScopeDoesNotMatch() {
        templating.start(scope)
        assertFalse(templating.isEnabledWith(TestTemplatingScope()))
    }

    @Test
    fun testIsEnabledWithReturnsFalseAfterStop() {
        templating.start(scope)
        templating.stop()
        assertFalse(templating.isEnabledWith(scope))
    }
}
